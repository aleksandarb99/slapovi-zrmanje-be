package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.shared.mapper.CampRequestMapper;
import com.slapovizrmanje.shared.model.Notification;
import com.slapovizrmanje.shared.model.enums.NotificationType;
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
    private final CampRequestMapper campRequestMapper;

    private static final String INSERT = "INSERT";
    private static final String MODIFY = "MODIFY";

    public Function<DynamodbEvent, DynamodbEvent> handleDynamoStreamEvent() {
        return dynamodbEvent -> {
            dynamodbEvent.getRecords().forEach(record -> {

                Notification notification = null;
                Map<String, AttributeValue> streamRecord = record.getDynamodb().getNewImage();
                String recordId = streamRecord.get("id").getS();

                if (recordId.startsWith("camp-request")) {
                    log.info("CAMP REQUEST MAPPER - Dynamodb event to camp request notification.");
                    notification = campRequestMapper.eventToNotification(streamRecord);
                    if (record.getEventName().equals(INSERT)) {
                        log.info(String.format("Notification type set to %s", NotificationType.CAMP_REQUEST));
                        notification.setType(NotificationType.CAMP_REQUEST);
                    } else if(record.getEventName().equals(MODIFY)) {
                        if (notification.isVerified()) {
                            log.info(String.format("Notification type set to %s", NotificationType.CAMP_VERIFY));
                            notification.setType(NotificationType.CAMP_VERIFY);
                        } else {
                            log.info("Modification type still not handled!");
                            return;
                        }
                        // TODO Add other modification types e.g. CAMP_ACCEPT, CAMP_REJECT (Determine based on modified attributes)
                    } else {
                        log.info("Not supported event type!!!");
                        return;
                    }
                } else if (recordId.startsWith("apartment-request")) {
                    log.info("Apartment type still not handled!");
                } else if (recordId.startsWith("room-request")) {
                    log.info("Room type still not handled!");
                } else {
                    log.info(String.format("Not known id %s", recordId));
                    return;
                }

                sendMessage(notification);
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
