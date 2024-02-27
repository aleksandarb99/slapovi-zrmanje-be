package com.slapovizrmanje.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.shared.dao.AccommodationDao;
import com.slapovizrmanje.shared.model.Accommodation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Component
@Slf4j
public class ReminderSenderComponent {

  private AccommodationDao accommodationDao;
  private final ObjectMapper objectMapper;
  private final SqsClient sqsClient;

  public Function<Object, Object> sendReminder() {
    return scheduledEvent -> {
      log.info("ACCOMMODATION DAO - Fetching entity where start date is tomorrow and state is reserved.");
      List<Accommodation> accommodationList = accommodationDao.findWhereStartDateIsTomorrowAndStateIsReserved();

      sendMessageToQueue(accommodationList);
      log.info("Reminding owner of accommodation request which will start date is tomorrow.");
      return scheduledEvent;
    };
  }

  private void sendMessageToQueue(final List<Accommodation> accommodations) {
    String queueUrl = getQueueUrl();
    try {
      String message = objectMapper.writeValueAsString(accommodations);
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
