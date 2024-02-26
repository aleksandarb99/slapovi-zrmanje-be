package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.shared.mapper.AccommodationMapper;
import com.slapovizrmanje.shared.model.Accommodation;
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
  private final AccommodationMapper accommodationMapper;

  public Function<DynamodbEvent, DynamodbEvent> handleDynamoStreamEvent() {
    return dynamodbEvent -> {
      dynamodbEvent.getRecords().forEach(record -> {
        Map<String, AttributeValue> streamRecord = record.getDynamodb().getNewImage();
        log.info("ACCOMMODATION MAPPER - Dynamodb event to accommodation.");
        Accommodation accommodation = accommodationMapper.eventToAccommodation(streamRecord);

        sendMessageToQueue(accommodation);
      });
      return dynamodbEvent;
    };
  }

  private void sendMessageToQueue(final Accommodation accommodation) {
    String queueUrl = getQueueUrl();
    try {
      String message = objectMapper.writeValueAsString(accommodation);
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
