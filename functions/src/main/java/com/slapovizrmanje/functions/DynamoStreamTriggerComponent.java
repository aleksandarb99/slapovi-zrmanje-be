package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.shared.mapper.AccommodationMapper;
import com.slapovizrmanje.shared.model.Accommodation;
import com.slapovizrmanje.shared.service.AwsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
@AllArgsConstructor
public class DynamoStreamTriggerComponent {

  private final AwsService awsService;
  private final ObjectMapper objectMapper;
  private final AccommodationMapper accommodationMapper;

  public Function<DynamodbEvent, DynamodbEvent> handleDynamoStreamEvent() {
    return dynamodbEvent -> {
      dynamodbEvent.getRecords().forEach(record -> {
        Map<String, AttributeValue> streamRecord = record.getDynamodb().getNewImage();
        log.info("ACCOMMODATION MAPPER - Dynamodb event to accommodation.");
        Accommodation accommodation = accommodationMapper.eventToAccommodation(streamRecord);

        String message;
        try {
          log.info("OBJECT MAPPER - Accommodation to String.");
          message = objectMapper.writeValueAsString(accommodation);
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Error occurred while converting Accommodation to String.");
        }
        log.info("AWS SERVICE - Sending accommodation to email queue.");
        awsService.sendMessageToQueue(message, accommodation.getClass().getSimpleName());
      });
      return dynamodbEvent;
    };
  }
}
