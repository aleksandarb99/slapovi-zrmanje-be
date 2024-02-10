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
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.io.File;
import java.util.List;

public class CdkStack extends Stack {
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
        final Table requestTable = new Table(scope, "request-table", TableProps.builder()
                .tableName("request-table")
                .partitionKey(email)
                .sortKey(id)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .stream(StreamViewType.NEW_IMAGE)
                .build());
//        requestTable.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
//                .indexName("GS1")
//                .partitionKey(sk)
//                .sortKey(pk)
//                .projectionType(ProjectionType.ALL)
//                .build());
//
//        final Attribute email = Attribute.builder()
//                .name("email")
//                .type(AttributeType.STRING)
//                .build();
//        celebrateTable.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
//                .indexName("EmailGSI")
//                .partitionKey(email)
//                .sortKey(sk)
//                .projectionType(ProjectionType.ALL)
//                .build());
        return requestTable.getTableArn();
    }

    public static PolicyStatement getDynamoReadStatement(final String tableArn, final String sid) {
        return new PolicyStatement(PolicyStatementProps.builder()
                .sid(sid)
                .effect(Effect.ALLOW)
                .actions(List.of("dynamodb:GetItem", "dynamodb:Query", "dynamodb:Scan"))
                .resources(List.of(tableArn))
                .build());
    }

    public static PolicyStatement getDynamoWriteStatement(final String tableArn, final String sid) {
        return new PolicyStatement(PolicyStatementProps.builder()
                .sid(sid)
                .effect(Effect.ALLOW)
                .actions(List.of("dynamodb:PutItem", "dynamodb:UpdateItem", "dynamodb:DeleteItem", "dynamodb:BatchWriteItem"))
                .resources(List.of(tableArn))
                .build());
    }
}
