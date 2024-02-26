package com.slapovizrmanje.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class CdkApp {
  public static void main(final String[] args) {
    final App app = new App();

//        final String stage = System.getenv("DEPLOY_STAGE");
//
//        if (stage == null || stage.equals("")) {
//            throw new RuntimeException("Please ensure that the \"DEPLOY_STAGE\" environment variable is set to a valid value.");
//        }

    new CdkStack(app, StackProps.builder()
            .env(Environment.builder()
                    .region("eu-central-1")
                    .build())
            .build());

    app.synth();
  }
}
