package com.slapovizrmanje.cdk;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.io.File;

public class CdkStack extends Stack {
    public CdkStack(final Construct scope, final StackProps props) {
        super(scope, "slapovi-zrmanje-be", props);

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
}
