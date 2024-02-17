package com.slapovizrmanje.functions;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
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
//        testEmailLambda();
        testDynamoLambda();
    }

    // When needed similar can be added for DynamoStream Lambda
    private static void testEmailLambda() {
        AmazonSimpleEmailService sesClient = AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
        EmailNotificationSenderComponent emailNotificationSenderComponent = new EmailNotificationSenderComponent(sesClient, new ObjectMapper());
        Function<SQSEvent, SQSEvent> emailLambda = emailNotificationSenderComponent.sendEmailNotification();

        // Create SQSMessage
        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setMessageId("1");
        sqsMessage.setBody("{\"emailAddress\": \"test@test.com\", \"emailType\": \"CAMP_REQUEST\", \"recordId\": \"testRecordId123\"}");

        // Create SQSEvent
        SQSEvent sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        emailLambda.apply(sqsEvent);
    }

    private static void testDynamoLambda() {
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
        DynamoStreamTriggerComponent dynamoStreamTriggerComponent = new DynamoStreamTriggerComponent(sqsClient, new ObjectMapper());
        Function<DynamodbEvent, DynamodbEvent> dynamoLambda = dynamoStreamTriggerComponent.handleDynamoStreamEvent();

        AttributeValue id = new AttributeValue().withS("kjsns-sjndj-asdas-snjds");
        AttributeValue email = new AttributeValue().withS("test@test.com");
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("id", id);
        map.put("email", email);
        StreamRecord streamRecord = new StreamRecord();
        streamRecord.setNewImage(map);

        // Create DynamodbStreamRecord
        DynamodbEvent.DynamodbStreamRecord record = new DynamodbEvent.DynamodbStreamRecord();
        record.setEventName("INSERT");
        record.setDynamodb(streamRecord);

        // Create DynamodbEvent
        DynamodbEvent dynamodbEvent = new DynamodbEvent();
        dynamodbEvent.setRecords(List.of(record));

        dynamoLambda.apply(dynamodbEvent);
    }
}
