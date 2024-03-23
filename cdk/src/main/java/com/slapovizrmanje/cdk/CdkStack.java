package com.slapovizrmanje.cdk;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.ProxyResourceOptions;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.cloudwatch.Alarm;
import software.amazon.awscdk.services.cloudwatch.ComparisonOperator;
import software.amazon.awscdk.services.cloudwatch.actions.SnsAction;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.events.CronOptions;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.PolicyStatementProps;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.eventsources.DynamoEventSource;
import software.amazon.awscdk.services.lambda.eventsources.SqsDlq;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CdkStack extends Stack {

  private Table accommodationTable = null;

  public CdkStack(Construct scope, StackProps props, String frontendUrl) {
    super(scope, "slapovi-zrmanje-be", props);

    // Create tables
    String accommodationTableArn = createAccommodationTable(this);

    // API Lambda
    SnapStartFunction apiFunction = generateApiLambda(accommodationTableArn, frontendUrl);

    // Proxy config
    ProxyResourceOptions proxyR = generateProxyConfiguration(apiFunction);

//        RestApiProps restApiProps = RestApiProps.builder()
//                .deployOptions(StageOptions.builder().stageName(stage).build())
//                .build();

    // Rest Api
    RestApi restApi = generateRestApi(proxyR);

    // Email Dead Letter Queue
    Queue emailDlQueue = generateEmailDeadLetterQueue("email-notifications-dl-queue");

    // Configure dead-letter queue settings
    DeadLetterQueue deadLetterQueueConfiguration = configureDeadLetterQueue(emailDlQueue);

    // Email Simple Queue Service (SQS)
    Queue emailQueue = generateEmailQueue(deadLetterQueueConfiguration, "email-notifications-sqs-queue");

    SqsEventSource emailSqsEventSource = SqsEventSource.Builder
            .create(emailQueue)
            .build();

    // Email Handler Lambda
    generateEmailHandlerLambda(apiFunction, emailQueue, emailSqsEventSource, frontendUrl);

    // Stream Event Dead Letter Queue Service (DlSQS)
    DynamoEventSource dynamoEventSource = createDynamoEventSoruce(frontendUrl);

    // Dynamo Handler Lambda
    generateDynamoHandlerLambda(emailQueue, dynamoEventSource, frontendUrl);

    // Create an Email SNS topic
    Topic emailSnsTopic = createSnsTopic();

    // Create Email Alarm based on messages visible in Email DLQ
    createEmailAlarm(emailDlQueue, emailSnsTopic);

    // Reminder
    generateReminderLambda(accommodationTableArn, emailQueue, frontendUrl);

    // ProposeDate
    generateProposeDateLambda(accommodationTableArn, emailQueue, frontendUrl);
  }

  @NotNull
  private static ProxyResourceOptions generateProxyConfiguration(SnapStartFunction apiFunction) {
    LambdaIntegration integration = new LambdaIntegration(apiFunction.getAlias());
    return ProxyResourceOptions.builder()
            .anyMethod(true)
            .defaultIntegration(integration)
            .build();
  }

  private SnapStartFunction generateApiLambda(String accommodationTableArn, String frontendUrl) {
    SnapStartFunction apiFunction = new SnapStartFunction(this, "slapovi-zrmanje-lambda",
            FunctionProps.builder()
                    .functionName("slapovi-zrmanje-lambda")
                    .runtime(Runtime.JAVA_17)
                    .code(Code.fromAsset(new File(new File(System.getProperty("user.dir")), "./api/target/api.jar").toString()))
                    .handler("com.slapovizrmanje.api.StreamLambdaHandler")
                    .environment(Map.of(
                            "FRONTEND_URL", frontendUrl
                    ))
                    .memorySize(2048)
                    .timeout(Duration.seconds(30))
                    .build());
    apiFunction.getFunction().addToRolePolicy(getDynamoReadStatement(accommodationTableArn, "AllowRequestTableRead"));
    apiFunction.getFunction().addToRolePolicy(getDynamoWriteStatement(accommodationTableArn, "AllowRequestTableWrite"));

    apiFunction.getAlias().grantInvoke(new ServicePrincipal("apigateway.amazonaws.com"));

    return apiFunction;
  }

  @NotNull
  private RestApi generateRestApi(ProxyResourceOptions proxyR) {
    RestApi restApi = new RestApi(this, "slapovi-zrmanje-rest-api");
    restApi.getRoot().addProxy(proxyR);
    return restApi;
  }

  @NotNull
  private Queue generateEmailDeadLetterQueue(String emailDlQueueName) {
    Queue emailDlQueue = Queue.Builder
            .create(this, emailDlQueueName)
            .queueName(emailDlQueueName)
            .retentionPeriod(Duration.days(14))
            .build();
    return emailDlQueue;
  }

  @NotNull
  private static DeadLetterQueue configureDeadLetterQueue(Queue emailDlQueue) {
    DeadLetterQueue deadLetterQueueConfiguration = DeadLetterQueue.builder()
            .queue(emailDlQueue)
            // TODO increase if needed
            .maxReceiveCount(1) // Maximum number of receives before moving to email dead letter queue
            .build();
    return deadLetterQueueConfiguration;
  }

  @NotNull
  private Queue generateEmailQueue(DeadLetterQueue deadLetterQueueConfiguration, String emailQueueName) {
    return Queue.Builder
            .create(this, emailQueueName)
            .queueName(emailQueueName)
            .deadLetterQueue(deadLetterQueueConfiguration)
            .visibilityTimeout(Duration.seconds(30))
            .build();
  }

  private void generateEmailHandlerLambda(SnapStartFunction apiFunction, Queue emailQueue, SqsEventSource emailSqsEventSource, String frontendUrl) {
    String emailLambdaName = "email-lambda";
    Function emailLambda = new Function(this, emailLambdaName,
            FunctionProps.builder()
                    .functionName(emailLambdaName)
                    .runtime(Runtime.JAVA_17)
                    .code(Code.fromAsset(new File(new File(System.getProperty("user.dir")), "./functions/target/functions.jar").toString()))
                    .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                    .environment(Map.of(
                            "SPRING_CLOUD_FUNCTION_DEFINITION", "sendEmailNotification",
                            "MAIN_CLASS", "com.slapovizrmanje.functions.FunctionsConfig",
                            "FRONTEND_URL", frontendUrl
                    ))
                    .memorySize(512)
                    .timeout(Duration.seconds(15))
                    .build());

    emailLambda.addEventSource(emailSqsEventSource);
    emailLambda.addToRolePolicy(getSesSendEmailStatement("AllowSendingEmail"));
    apiFunction.getFunction().addToRolePolicy(getSqsGetSendStatement(emailQueue.getQueueArn(), "AllowSqsQueueGetSend"));
  }

  private void generateDynamoHandlerLambda(Queue emailQueue, DynamoEventSource dynamoEventSource, String frontendUrl) {
    String dynamoLambdaName = "dynamo-stream-trigger-lambda";
    Function dynamoTriggerLambda = new Function(this, dynamoLambdaName,
            FunctionProps.builder()
                    .functionName(dynamoLambdaName)
                    .runtime(Runtime.JAVA_17)
                    .code(Code.fromAsset(new File(new File(System.getProperty("user.dir")), "./functions/target/functions.jar").toString()))
                    .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                    .environment(Map.of(
                            "SPRING_CLOUD_FUNCTION_DEFINITION", "handleDynamoStreamEvent",
                            "MAIN_CLASS", "com.slapovizrmanje.functions.FunctionsConfig",
                            "FRONTEND_URL", frontendUrl
                    ))
                    .memorySize(512)
                    .timeout(Duration.seconds(15))
                    .build());

    dynamoTriggerLambda.addEventSource(dynamoEventSource);
    dynamoTriggerLambda.addToRolePolicy(getDynamoStreamsStatement(accommodationTable.getTableStreamArn(), "AllowDynamoStreams"));
    dynamoTriggerLambda.addToRolePolicy(getSqsGetSendStatement(emailQueue.getQueueArn(), "AllowSqsQueueGetSend"));
  }

  @NotNull
  private DynamoEventSource createDynamoEventSoruce(String frontendUrl) {
    String streamEventDlQueueName = "stream-event-dl-queue";
    Queue streamEventQueue = Queue.Builder
            .create(this, streamEventDlQueueName)
            .queueName(streamEventDlQueueName)
            .retentionPeriod(Duration.days(14))
            .build();
    SqsDlq streamEventDlQueue = new SqsDlq(streamEventQueue);

    DynamoEventSource dynamoEventSource = DynamoEventSource.Builder
            .create(accommodationTable)
            .batchSize(1)
            .startingPosition(StartingPosition.LATEST)
            .onFailure(streamEventDlQueue)
            .retryAttempts(1)
            .build();
    return dynamoEventSource;
  }

  @NotNull
  private Topic createSnsTopic() {
    Topic emailSnsTopic = Topic.Builder.create(this, "EmailSnsTopic")
            .displayName("Email SNS Topic")
            .topicName("email-sns-topic")
            .build();

    // Create email subscriptions for devs/ supports
    EmailSubscription emailSubscription = EmailSubscription.Builder.create("jovansimic995@gmail.com").build();
    emailSnsTopic.addSubscription(emailSubscription);
    return emailSnsTopic;
  }

  private void createEmailAlarm(Queue emailDlQueue, Topic emailSnsTopic) {
    Alarm emailAlarm = Alarm.Builder
            .create(this, "EmailAlarm")
            .alarmName("Email Dead Letter Queue Alarm")
            .alarmDescription("Alarm Invocation on message being sent to Email DLQ")
            .datapointsToAlarm(1)
            .evaluationPeriods(1)
            .threshold(20)
            .metric(emailDlQueue.metricApproximateNumberOfMessagesVisible())
            .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD)
            .build();

    emailAlarm.addAlarmAction(new SnsAction(emailSnsTopic));
  }

  private void generateReminderLambda(String accommodationTableArn, Queue emailQueue, String frontendUrl) {
    SnapStartFunction reminderSenderFunction = new SnapStartFunction(this, "reminder-lambda",
            FunctionProps.builder()
                    .functionName("reminder-lambda")
                    .runtime(Runtime.JAVA_17)
                    .code(Code.fromAsset(new File(new File(System.getProperty("user.dir")), "./functions/target/functions.jar").toString()))
                    .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                    .environment(Map.of(
                            "SPRING_CLOUD_FUNCTION_DEFINITION", "sendReminder",
                            "MAIN_CLASS", "com.slapovizrmanje.functions.FunctionsConfig",
                            "FRONTEND_URL", frontendUrl
                    ))
                    .memorySize(512)
                    .timeout(Duration.seconds(15))
                    .build());
    reminderSenderFunction.getFunction().addToRolePolicy(getDynamoReadStatement(accommodationTableArn, "AllowRequestTableRead"));
    reminderSenderFunction.getFunction().addToRolePolicy(getSqsGetSendStatement(emailQueue.getQueueArn(), "AllowSqsQueueGetSend"));

    reminderSenderFunction.getFunction().grantInvoke(new ServicePrincipal("events.amazonaws.com"));

    Schedule reminderSchedule = Schedule.cron(CronOptions.builder()
            .minute("0").hour("20").day("*").month("*").year("*").build());
    Rule reminderRule = Rule.Builder.create(this, "reminder-rule")
            .ruleName("reminder-rule")
            .enabled(false)
            .schedule(reminderSchedule)
            .targets(List.of(LambdaFunction.Builder.create(reminderSenderFunction.getFunction()).build()))
            .build();
  }

  private void generateProposeDateLambda(String accommodationTableArn, Queue emailQueue, String frontendUrl) {
    SnapStartFunction proposeDateFunction = new SnapStartFunction(this, "propose-date-lambda",
            FunctionProps.builder()
                    .functionName("propose-date-lambda")
                    .runtime(Runtime.JAVA_17)
                    .code(Code.fromAsset(new File(new File(System.getProperty("user.dir")), "./functions/target/functions.jar").toString()))
                    .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                    .environment(Map.of(
                            "SPRING_CLOUD_FUNCTION_DEFINITION", "proposeDate",
                            "MAIN_CLASS", "com.slapovizrmanje.functions.FunctionsConfig",
                            "FRONTEND_URL", frontendUrl
                    ))
                    .memorySize(512)
                    .timeout(Duration.seconds(15))
                    .build());
    proposeDateFunction.getFunction().addToRolePolicy(getDynamoReadStatement(accommodationTableArn, "AllowRequestTableRead"));
    proposeDateFunction.getFunction().addToRolePolicy(getSqsGetSendStatement(emailQueue.getQueueArn(), "AllowSqsQueueGetSend"));

    proposeDateFunction.getFunction().grantInvoke(new ServicePrincipal("events.amazonaws.com"));

    Schedule proposeDateSchedule = Schedule.cron(CronOptions.builder()
            .minute("30").hour("20").day("*").month("*").year("*").build());
    Rule proposeDateRule = Rule.Builder.create(this, "propose-date-rule")
            .ruleName("propose-date-rule")
            .enabled(false)
            .schedule(proposeDateSchedule)
            .targets(List.of(LambdaFunction.Builder.create(proposeDateFunction.getFunction()).build()))
            .build();
  }

  private String createAccommodationTable(Construct scope) {
    Attribute email = Attribute.builder()
            .name("email")
            .type(AttributeType.STRING)
            .build();
    Attribute id = Attribute.builder()
            .name("id")
            .type(AttributeType.STRING)
            .build();
    accommodationTable = new Table(scope, "accommodation", TableProps.builder()
            .tableName("accommodation")
            .partitionKey(email)
            .sortKey(id)
            .billingMode(BillingMode.PAY_PER_REQUEST)
            .stream(StreamViewType.NEW_IMAGE)
            .build());
    return accommodationTable.getTableArn();
  }

  private PolicyStatement getDynamoReadStatement(String tableArn, String sid) {
    return new PolicyStatement(PolicyStatementProps.builder()
            .sid(sid)
            .effect(Effect.ALLOW)
            .actions(List.of("dynamodb:GetItem", "dynamodb:Query", "dynamodb:Scan"))
            .resources(List.of(tableArn))
            .build());
  }

  private PolicyStatement getDynamoWriteStatement(String tableArn, String sid) {
    return new PolicyStatement(PolicyStatementProps.builder()
            .sid(sid)
            .effect(Effect.ALLOW)
            .actions(List.of("dynamodb:PutItem", "dynamodb:UpdateItem", "dynamodb:DeleteItem", "dynamodb:BatchWriteItem"))
            .resources(List.of(tableArn))
            .build());
  }

  private PolicyStatement getSesSendEmailStatement(String sid) {
    return new PolicyStatement(PolicyStatementProps.builder()
            .sid(sid)
            .effect(Effect.ALLOW)
            .actions(List.of("ses:SendEmail"))
            .resources(List.of("*"))
            .build());
  }

  private PolicyStatement getDynamoStreamsStatement(String streamArn, String sid) {
    return new PolicyStatement(PolicyStatementProps.builder()
            .sid(sid)
            .effect(Effect.ALLOW)
            .actions(List.of("dynamodb:DescribeStream", "dynamodb:GetRecords", "dynamodb:GetShardIterator", "dynamodb:ListStreams"))
            .resources(List.of(streamArn))
            .build());
  }

  private static PolicyStatement getSqsGetSendStatement(String queueArn, String sid) {
    return new PolicyStatement(PolicyStatementProps.builder()
            .sid(sid)
            .effect(Effect.ALLOW)
            .actions(List.of("sqs:SendMessage", "sqs:GetQueueUrl", "sqs:GetQueueAttributes"))
            .resources(List.of(queueArn))
            .build());
  }
}
