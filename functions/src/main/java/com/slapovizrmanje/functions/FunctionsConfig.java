package com.slapovizrmanje.functions;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.function.Function;

@ComponentScan(basePackages = {"com.slapovizrmanje"})
@SpringBootApplication
public class FunctionsConfig {

    @Bean
    public Function<SQSEvent, SQSEvent> sendEmailNotification(final EmailNotificationSenderComponent emailNotificationSenderComponent) {
        return emailNotificationSenderComponent.sendEmailNotification();
    }

    @Bean
    public Function<DynamodbEvent, DynamodbEvent> handleDynamoStreamEvent(final DynamoStreamTriggerComponent dynamoStreamTriggerComponent) {
        return dynamoStreamTriggerComponent.handleDynamoStreamEvent();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
    }

    @Bean
    public AmazonSimpleEmailService sesClient() {
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
    }

    public static void main(final String[] args) {
    }

}
