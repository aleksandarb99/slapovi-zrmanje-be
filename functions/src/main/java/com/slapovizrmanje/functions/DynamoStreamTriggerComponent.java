package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.model.Notification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
@AllArgsConstructor
public class DynamoStreamTriggerComponent {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    public Function<DynamodbEvent, DynamodbEvent> handleDynamoStreamEvent() {
        return dynamodbEvent -> {
            dynamodbEvent.getRecords().forEach(record -> {
                if (record.getEventName().equals("INSERT")) {
                    Map<String, AttributeValue> streamRecord = record.getDynamodb().getNewImage();
                    String recordId = streamRecord.get("id").getS();
                    String emailAddress = streamRecord.get("email").getS();
                    String emailType = "CAMP_REQUEST";
                    Notification campRequestNotification = Notification.builder()
                            .recordId(recordId)
                            .emailAddress(emailAddress)
                            .emailType(emailType)
                            .build();
                    sendMessage(campRequestNotification);
                } else {
                    log.info(String.format("Currently type of dynamo event - %s is not handled!.", record.getEventName()));
                }
            });
            return dynamodbEvent;
        };
    }

    private void sendMessage(final Notification notification) {
        String queueUrl = getQueueUrl();
        try {
            String message = objectMapper.writeValueAsString(notification);
            log.info(String.format("Sending a message to SQS - %s.", message));
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .build());
            log.info("SQS message has been sent.");
        } catch (final Exception e) {
            log.error("Error while sending the message to SQS.", e);
            throw new RuntimeException("Error while sending the message to SQS.");
        }
    }

    private String getQueueUrl() {
        String queueUrl;
        try {
            GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest
                    .builder()
                    .queueName("email-notifications-sqs-queue")
                    .build();
            log.info("Fetching a queue url for - email-notifications-sqs-queue.");
            queueUrl = sqsClient.getQueueUrl(getQueueUrlRequest).queueUrl();
            log.info("Queue url - email-notifications-sqs-queue has been fetched.");
        } catch (final Exception e) {
            log.error("Error while fetching the queue url.", e);
            throw new RuntimeException("Error while fetching the queue url.");
        }
        return queueUrl;
    }
}
