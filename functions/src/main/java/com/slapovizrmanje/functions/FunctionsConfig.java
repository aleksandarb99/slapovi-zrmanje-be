package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

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

    public static void main(final String[] args) {
    }

}
