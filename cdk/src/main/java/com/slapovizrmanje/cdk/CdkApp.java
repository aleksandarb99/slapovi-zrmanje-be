package com.slapovizrmanje.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class CdkApp {
  public static void main(final String[] args) {
    App app = new App();

    String frontendUrl = getEnvOrThrowException("FRONTEND_URL");

    new CdkStack(app, StackProps.builder()
            .env(Environment.builder()
                    .region("eu-central-1")
                    .build())
            .build(), frontendUrl);

    app.synth();
  }

  private static String getEnvOrThrowException(String envName) {
    String envValue = System.getenv(envName);
    if (envValue == null || envValue.equals("")) {
      throw new RuntimeException(String.format("Please ensure that the \"%s\" environment variable is set to a valid value.", envName));
    }
    return envValue;
  }
}
