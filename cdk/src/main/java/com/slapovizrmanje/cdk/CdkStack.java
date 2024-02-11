package com.slapovizrmanje.cdk;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.PolicyStatementProps;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.DynamoEventSource;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CdkStack extends Stack {

    private Table requestTable = null;

    public CdkStack(final Construct scope, final StackProps props) {
        super(scope, "slapovi-zrmanje-be", props);

        // Create tables
        String requestTableArn = createRequestTable(this);

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
        apiFunction.getFunction().addToRolePolicy(getDynamoReadStatement(requestTableArn, "AllowRequestTableRead"));
        apiFunction.getFunction().addToRolePolicy(getDynamoWriteStatement(requestTableArn, "AllowRequestTableWrite"));

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

        // Email Simple Queue Service (SQS)
        final String emailQueueName = "email-notifications-sqs-queue";
        final Queue emailQueue = Queue.Builder
                .create(this, emailQueueName)
                .queueName(emailQueueName)
//                .deadLetterQueue()
                .visibilityTimeout(Duration.seconds(30))
                .retentionPeriod(Duration.days(14))
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

        final SqsEventSource emailSqsEventSource = SqsEventSource.Builder.create(emailQueue).build();
        emailLambda.addEventSource(emailSqsEventSource);

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

        final DynamoEventSource dynamoEventSource = DynamoEventSource.Builder
                .create(requestTable)
                .batchSize(1)   // TODO Batch size check
                .startingPosition(StartingPosition.LATEST)  // TODO Check this latest
//                .retryAttempts()
//                .onFailure()  // TODO Check other properties
                .build();
        dynamoTriggerLambda.addEventSource(dynamoEventSource);
        dynamoTriggerLambda.addToRolePolicy(getDynamoStreamsStatement(requestTable.getTableStreamArn(), "AllowDynamoStreams"));
    }

    private String createRequestTable(Construct scope) {
        final Attribute email = Attribute.builder()
                .name("email")
                .type(AttributeType.STRING)
                .build();
        final Attribute id = Attribute.builder()
                .name("id")
                .type(AttributeType.STRING)
                .build();
        requestTable = new Table(scope, "request-table", TableProps.builder()
                .tableName("request-table")
                .partitionKey(email)
                .sortKey(id)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .stream(StreamViewType.NEW_IMAGE)
                .build());
        return requestTable.getTableArn();
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

    private PolicyStatement getDynamoStreamsStatement(final String streamArn, final String sid) {
        return new PolicyStatement(PolicyStatementProps.builder()
                .sid(sid)
                .effect(Effect.ALLOW)
                .actions(List.of("dynamodb:DescribeStream", "dynamodb:GetRecords", "dynamodb:GetShardIterator", "dynamodb:ListStreams"))
                .resources(List.of(streamArn))
                .build());
    }
}
