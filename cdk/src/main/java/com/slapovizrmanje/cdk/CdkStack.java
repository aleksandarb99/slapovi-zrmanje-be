package com.slapovizrmanje.cdk;

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

  public CdkStack(final Construct scope, final StackProps props) {
    super(scope, "slapovi-zrmanje-be", props);

    // Create tables
    String accommodationTableArn = createAccommodationTable(this);

    // API Gateway and Lambda
    final SnapStartFunction apiFunction = new SnapStartFunction(this, "slapovi-zrmanje-lambda",
            FunctionProps.builder()
                    .functionName("slapovi-zrmanje-lambda")
                    .runtime(Runtime.JAVA_17)
                    .code(Code.fromAsset(new File(new File(System.getProperty("user.dir")), "./api/target/api.jar").toString()))
                    .handler("com.slapovizrmanje.api.StreamLambdaHandler")
                    .memorySize(2048)
                    .timeout(Duration.seconds(30))
                    .build());
    apiFunction.getFunction().addToRolePolicy(getDynamoReadStatement(accommodationTableArn, "AllowRequestTableRead"));
    apiFunction.getFunction().addToRolePolicy(getDynamoWriteStatement(accommodationTableArn, "AllowRequestTableWrite"));

    apiFunction.getAlias().grantInvoke(new ServicePrincipal("apigateway.amazonaws.com"));

    final LambdaIntegration integration = new LambdaIntegration(apiFunction.getAlias());

    final ProxyResourceOptions proxyR = ProxyResourceOptions.builder()
            .anyMethod(true)
            .defaultIntegration(integration)
            .build();

//        final RestApiProps restApiProps = RestApiProps.builder()
//                .deployOptions(StageOptions.builder().stageName(stage).build())
//                .build();
    final RestApi restApi = new RestApi(this, "slapovi-zrmanje-rest-api");

    restApi.getRoot().addProxy(proxyR);

    // Email Dead Letter Queue
    final String emailDlQueueName = "email-notifications-dl-queue";
    final Queue emailDlQueue = Queue.Builder
            .create(this, emailDlQueueName)
            .queueName(emailDlQueueName)
            .retentionPeriod(Duration.days(14))
            .build();

    // Configure dead-letter queue settings
    final DeadLetterQueue deadLetterQueueConfiguration = DeadLetterQueue.builder()
            .queue(emailDlQueue)
            // TODO increase if needed
            .maxReceiveCount(1) // Maximum number of receives before moving to email dead letter queue
            .build();

    // Email Simple Queue Service (SQS)
    final String emailQueueName = "email-notifications-sqs-queue";
    final Queue emailQueue = Queue.Builder
            .create(this, emailQueueName)
            .queueName(emailQueueName)
            .deadLetterQueue(deadLetterQueueConfiguration)
            .visibilityTimeout(Duration.seconds(30))
            .build();

    // Email Handler Lambda
    final String emailLambdaName = "email-lambda";
    // TODO make it snapstart
    Function emailLambda = new Function(this, emailLambdaName,
            FunctionProps.builder()
                    .functionName(emailLambdaName)
                    .runtime(Runtime.JAVA_17)
                    .code(Code.fromAsset(new File(new File(System.getProperty("user.dir")), "./functions/target/functions.jar").toString()))
                    .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                    .environment(Map.of(
                            "SPRING_CLOUD_FUNCTION_DEFINITION", "sendEmailNotification",
                            "MAIN_CLASS", "com.slapovizrmanje.functions.FunctionsConfig"
                    ))
                    .memorySize(512)
                    .timeout(Duration.seconds(15))
                    .build());

    final SqsEventSource emailSqsEventSource = SqsEventSource.Builder
            .create(emailQueue)
            .build();
    emailLambda.addEventSource(emailSqsEventSource);
    emailLambda.addToRolePolicy(getSesSendEmailStatement("AllowSendingEmail"));
    apiFunction.getFunction().addToRolePolicy(getSqsGetSendStatement(emailQueue.getQueueArn(), "AllowSqsQueueGetSend"));

    // Dynamo Handler Lambda
    final String dynamoLambdaName = "dynamo-stream-trigger-lambda";
    // TODO make it snapstart
    Function dynamoTriggerLambda = new Function(this, dynamoLambdaName,
            FunctionProps.builder()
                    .functionName(dynamoLambdaName)
                    .runtime(Runtime.JAVA_17)
                    .code(Code.fromAsset(new File(new File(System.getProperty("user.dir")), "./functions/target/functions.jar").toString()))
                    .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                    .environment(Map.of(
                            "SPRING_CLOUD_FUNCTION_DEFINITION", "handleDynamoStreamEvent",
                            "MAIN_CLASS", "com.slapovizrmanje.functions.FunctionsConfig"
                    ))
                    .memorySize(512)
                    .timeout(Duration.seconds(15))
                    .build());

    // Stream Event Dead Letter Queue Service (DlSQS)
    final String streamEventDlQueueName = "stream-event-dl-queue";
    final Queue streamEventQueue = Queue.Builder
            .create(this, streamEventDlQueueName)
            .queueName(streamEventDlQueueName)
            .retentionPeriod(Duration.days(14))
            .build();
    SqsDlq streamEventDlQueue = new SqsDlq(streamEventQueue);

    final DynamoEventSource dynamoEventSource = DynamoEventSource.Builder
            .create(accommodationTable)
            .batchSize(1)
            .startingPosition(StartingPosition.LATEST)
            .onFailure(streamEventDlQueue)
            .retryAttempts(1)
            .build();

    dynamoTriggerLambda.addEventSource(dynamoEventSource);
    dynamoTriggerLambda.addToRolePolicy(getDynamoStreamsStatement(accommodationTable.getTableStreamArn(), "AllowDynamoStreams"));
    dynamoTriggerLambda.addToRolePolicy(getSqsGetSendStatement(emailQueue.getQueueArn(), "AllowSqsQueueGetSend"));

    // Create an Email SNS topic
    Topic emailSnsTopic = Topic.Builder.create(this, "EmailSnsTopic")
            .displayName("Email SNS Topic")
            .topicName("email-sns-topic")
            .build();

    // Create email subscriptions for devs/ supports
    EmailSubscription emailSubscription = EmailSubscription.Builder.create("jovansimic995@gmail.com").build();
    emailSnsTopic.addSubscription(emailSubscription);

    // Create Email Alarm based on messages visible in Email DLQ
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

  private String createAccommodationTable(Construct scope) {
    final Attribute email = Attribute.builder()
            .name("email")
            .type(AttributeType.STRING)
            .build();
    final Attribute id = Attribute.builder()
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

  private PolicyStatement getDynamoReadStatement(final String tableArn, final String sid) {
    return new PolicyStatement(PolicyStatementProps.builder()
            .sid(sid)
            .effect(Effect.ALLOW)
            .actions(List.of("dynamodb:GetItem", "dynamodb:Query", "dynamodb:Scan"))
            .resources(List.of(tableArn))
            .build());
  }

  private PolicyStatement getDynamoWriteStatement(final String tableArn, final String sid) {
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

  private PolicyStatement getDynamoStreamsStatement(final String streamArn, final String sid) {
    return new PolicyStatement(PolicyStatementProps.builder()
            .sid(sid)
            .effect(Effect.ALLOW)
            .actions(List.of("dynamodb:DescribeStream", "dynamodb:GetRecords", "dynamodb:GetShardIterator", "dynamodb:ListStreams"))
            .resources(List.of(streamArn))
            .build());
  }

  private static PolicyStatement getSqsGetSendStatement(final String queueArn, final String sid) {
    return new PolicyStatement(PolicyStatementProps.builder()
            .sid(sid)
            .effect(Effect.ALLOW)
            .actions(List.of("sqs:SendMessage", "sqs:GetQueueUrl", "sqs:GetQueueAttributes"))
            .resources(List.of(queueArn))
            .build());
  }
}
