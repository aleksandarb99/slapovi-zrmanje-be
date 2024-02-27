package com.slapovizrmanje.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsService {

    private final SqsClient sqsClient;

    public void sendMessageToQueue(final String message, final String className) {
        String queueUrl = getQueueUrl();
        try {
            SendMessageRequest sqsMessage = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .messageAttributes(Map.of("class", MessageAttributeValue.builder().stringValue(className).dataType("String").build()))
                    .build();
            log.info(String.format("SQS CLIENT - Sending a message to SQS - %s.", message));
            sqsClient.sendMessage(sqsMessage);
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
