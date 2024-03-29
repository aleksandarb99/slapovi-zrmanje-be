package com.slapovizrmanje.functions;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.slapovizrmanje.shared.mapper.AccommodationMapper;
import com.slapovizrmanje.shared.mapper.AccommodationMapperImpl;
import com.slapovizrmanje.shared.service.AwsService;
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
  public Function<Object, Object> proposeDate(final ProposeDateComponent proposeDateComponent) {
    return proposeDateComponent.proposeDate();
  }

  @Bean
  public Function<Object, Object> sendReminder(final ReminderSenderComponent reminderSenderComponent) {
    return reminderSenderComponent.sendReminder();
  }

  @Bean
  public Function<SQSEvent, SQSEvent> sendEmailNotification(final EmailNotificationSenderComponent emailNotificationSenderComponent) {
    return emailNotificationSenderComponent.sendEmailNotification();
  }

  @Bean
  public Function<DynamodbEvent, DynamodbEvent> handleDynamoStreamEvent(final DynamoStreamTriggerComponent dynamoStreamTriggerComponent) {
    return dynamoStreamTriggerComponent.handleDynamoStreamEvent();
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  @Bean
  public AmazonSimpleEmailService sesClient() {
    return AmazonSimpleEmailServiceClientBuilder.standard()
            .withRegion(Regions.EU_CENTRAL_1)
            .build();
  }

  public static void main(final String[] args) {
//    testEmailLambda();
//    testContactEmailLambda();
    testScheduledEmailLambda();
//    testDynamoLambda();
  }

  // When needed similar can be added for DynamoStream Lambda
  private static void testEmailLambda() {
    AmazonSimpleEmailService sesClient = AmazonSimpleEmailServiceClientBuilder.standard()
            .withRegion(Regions.EU_CENTRAL_1)
            .build();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    EmailNotificationSenderComponent emailNotificationSenderComponent = new EmailNotificationSenderComponent(sesClient, objectMapper);
    Function<SQSEvent, SQSEvent> emailLambda = emailNotificationSenderComponent.sendEmailNotification();

    // Create SQSMessage
    SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
    sqsMessage.setMessageId("1");
    sqsMessage.setBody("{\"email\": \"jovansimic995@gmail.com\", " +
            "\"type\": \"APARTMENT\", " +
            "\"state\": \"EMAIL_NOT_VERIFIED\", " +
            "\"language\": \"DE\", " +
            "\"firstName\": \"Jovan\", " +
            "\"lastName\": \"Simic\", " +
            "\"startDate\": \"2024-02-16\", " +
            "\"endDate\": \"2024-02-17\", " +
            "\"powerSupply\": true, " +
            "\"guests\": {\"adults\": 2, \"children\": 1, \"infants\": 0, \"pets\": 1}, " +
            "\"lodging\": {\"apartment1\": 0, \"apartment2\": 0, \"apartment3\": 1}, " +
//                "\"lodging\": {\"room1\": 1, \"room2\": 1, \"room3\": 0}, " +
//                "\"lodging\": {\"car\": 1, \"caravan\": 0, \"tent\": 1, \"sleepingBag\": 2}, " +
            "\"id\": \"shbds-sads-dgddd-sda-asdsd\"}");
    SQSEvent.MessageAttribute classAttribute = new SQSEvent.MessageAttribute();
    classAttribute.setStringValue("Accommodation");
    classAttribute.setDataType("String");
    sqsMessage.setMessageAttributes(Map.of("class", classAttribute));

    // Create SQSEvent
    SQSEvent sqsEvent = new SQSEvent();
    sqsEvent.setRecords(List.of(sqsMessage));

    emailLambda.apply(sqsEvent);
  }

  // When needed similar can be added for DynamoStream Lambda
  private static void testContactEmailLambda() {
    AmazonSimpleEmailService sesClient = AmazonSimpleEmailServiceClientBuilder.standard()
            .withRegion(Regions.EU_CENTRAL_1)
            .build();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    EmailNotificationSenderComponent emailNotificationSenderComponent = new EmailNotificationSenderComponent(sesClient, objectMapper);
    Function<SQSEvent, SQSEvent> emailLambda = emailNotificationSenderComponent.sendEmailNotification();

    // Create SQSMessage
    SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
    sqsMessage.setMessageId("1");
    sqsMessage.setBody("{\"email\": \"petrovic.ma9@gmail.com\", " +
            "\"name\": \"Marija Petrovic\", " +
            "\"message\": \"Kad imate dostupne smestaje, da vas ne smaram sa rezervacijama, vidim da nije moguce videti dostupnost?\"}");
    SQSEvent.MessageAttribute classAttribute = new SQSEvent.MessageAttribute();
    classAttribute.setStringValue("ContactQuestionDTO");
    classAttribute.setDataType("String");
    sqsMessage.setMessageAttributes(Map.of("class", classAttribute));

    // Create SQSEvent
    SQSEvent sqsEvent = new SQSEvent();
    sqsEvent.setRecords(List.of(sqsMessage));

    emailLambda.apply(sqsEvent);
  }

  private static void testScheduledEmailLambda() {
    AmazonSimpleEmailService sesClient = AmazonSimpleEmailServiceClientBuilder.standard()
            .withRegion(Regions.EU_CENTRAL_1)
            .build();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    EmailNotificationSenderComponent emailNotificationSenderComponent = new EmailNotificationSenderComponent(sesClient, objectMapper);
    Function<SQSEvent, SQSEvent> emailLambda = emailNotificationSenderComponent.sendEmailNotification();

    // Create SQSMessage
    SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
    sqsMessage.setMessageId("1");
    sqsMessage.setBody("{\"type\": \"REMINDER\", \"accommodations\": [" +
            "{\"email\": \"jovansimic995@gmail.com\", " +
            "\"type\": \"APARTMENT\", " +
            "\"state\": \"NOT_AVAILABLE\", " +
            "\"language\": \"EN\", " +
            "\"firstName\": \"Jovan\", " +
            "\"lastName\": \"Simic\", " +
            "\"startDate\": \"2024-02-16\", " +
            "\"endDate\": \"2024-02-17\", " +
            "\"powerSupply\": true, " +
            "\"guests\": {\"adults\": 2, \"children\": 1, \"infants\": 0, \"pets\": 1}, " +
            "\"lodging\": {\"apartment1\": 0, \"apartment2\": 0, \"apartment3\": 1}, " +
            "\"id\": \"shbds-sads-dgddd-sda-asdsd\"}," +
            "{\"email\": \"abuljevic8@gmail.com\", " +
            "\"type\": \"CAMP\", " +
            "\"state\": \"NOT_AVAILABLE\", " +
            "\"language\": \"EN\", " +
            "\"firstName\": \"Aleksandar\", " +
            "\"lastName\": \"Buljevic\", " +
            "\"startDate\": \"2024-02-16\", " +
            "\"endDate\": \"2024-02-17\", " +
            "\"powerSupply\": true, " +
            "\"guests\": {\"adults\": 2, \"children\": 1, \"infants\": 0, \"pets\": 1}, " +
            "\"lodging\": {\"car\": 1, \"caravan\": 1, \"sleepingBag\": 1}, " +
            "\"id\": \"shbds-sads-dgddd-sda-asdsd\"}," +
            "{\"email\": \"petrovic.ma9@gmail.com\", " +
            "\"type\": \"APARTMENT\", " +
            "\"state\": \"NOT_AVAILABLE\", " +
            "\"language\": \"EN\", " +
            "\"firstName\": \"Marija\", " +
            "\"lastName\": \"Petrovic\", " +
            "\"startDate\": \"2024-02-16\", " +
            "\"endDate\": \"2024-02-17\", " +
            "\"powerSupply\": true, " +
            "\"guests\": {\"adults\": 2, \"children\": 1, \"infants\": 0, \"pets\": 1}, " +
            "\"lodging\": {\"apartment1\": 1, \"apartment2\": 0, \"apartment3\": 0}, " +
            "\"id\": \"shbds-sads-dgddd-sda-asdsd\"}," +
            "{\"email\": \"ivana.korpak00@gmail.com\", " +
            "\"type\": \"APARTMENT\", " +
            "\"state\": \"NOT_AVAILABLE\", " +
            "\"language\": \"EN\", " +
            "\"firstName\": \"Ivana\", " +
            "\"lastName\": \"Korpak\", " +
            "\"startDate\": \"2024-02-16\", " +
            "\"endDate\": \"2024-02-17\", " +
            "\"powerSupply\": true, " +
            "\"guests\": {\"adults\": 2, \"children\": 1, \"infants\": 0, \"pets\": 1}, " +
            "\"lodging\": {\"apartment1\": 0, \"apartment2\": 1, \"apartment3\": 0}, " +
            "\"id\": \"shbds-sads-dgddd-sda-asdsd\"}" +
            "]}");
    SQSEvent.MessageAttribute classAttribute = new SQSEvent.MessageAttribute();
    classAttribute.setStringValue("AccommodationsDTO");
    classAttribute.setDataType("String");
    sqsMessage.setMessageAttributes(Map.of("class", classAttribute));

    // Create SQSEvent
    SQSEvent sqsEvent = new SQSEvent();
    sqsEvent.setRecords(List.of(sqsMessage));

    emailLambda.apply(sqsEvent);
  }

  private static void testDynamoLambda() {
    // AccommodationMapperImpl if not recognized -> ./mvnw clean package
    AccommodationMapper accommodationMapper = new AccommodationMapperImpl();
    SqsClient sqsClient = SqsClient.builder()
            .region(Region.EU_CENTRAL_1)
            .build();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    DynamoStreamTriggerComponent dynamoStreamTriggerComponent = new DynamoStreamTriggerComponent(new AwsService(sqsClient), objectMapper, accommodationMapper);
    Function<DynamodbEvent, DynamodbEvent> dynamoLambda = dynamoStreamTriggerComponent.handleDynamoStreamEvent();

    // Create Guests
    Map<String, AttributeValue> guestsMap = new HashMap<>();
    guestsMap.put("adults", new AttributeValue().withN("2"));
    guestsMap.put("children", new AttributeValue().withN("0"));
    guestsMap.put("infants", new AttributeValue().withN("0"));
    guestsMap.put("pets", new AttributeValue().withN("1"));

    // Create Lodging
    Map<String, AttributeValue> lodgingMap = new HashMap<>();
    lodgingMap.put("car", new AttributeValue().withN("1"));
    lodgingMap.put("caravan", new AttributeValue().withN("0"));
    lodgingMap.put("tent", new AttributeValue().withN("1"));
    lodgingMap.put("sleeping_bag", new AttributeValue().withN("0"));

    // Create final map
    Map<String, AttributeValue> finalMap = new HashMap<>();
    finalMap.put("id", new AttributeValue().withS("camp-request#kjsns-sjndj-asdas-snjds"));
    finalMap.put("email", new AttributeValue().withS("test@test.com"));
    finalMap.put("first_name", new AttributeValue().withS("Jovan"));
    finalMap.put("last_name", new AttributeValue().withS("Simic"));
    finalMap.put("verified", new AttributeValue().withBOOL(true));
    finalMap.put("power_supply", new AttributeValue().withBOOL(false));
    finalMap.put("created_at", new AttributeValue().withN("1707691825368"));
    finalMap.put("start_date", new AttributeValue().withS("2024-03-19"));
    finalMap.put("end_date", new AttributeValue().withS("2024-03-20"));
    finalMap.put("code", new AttributeValue().withS("testCode"));
    finalMap.put("state", new AttributeValue().withS("EMAIL_NOT_VERIFIED"));
    finalMap.put("type", new AttributeValue().withS("CAMP"));
    finalMap.put("language", new AttributeValue().withS("EN"));
    finalMap.put("guests", new AttributeValue().withM(guestsMap));
    finalMap.put("lodging", new AttributeValue().withM(lodgingMap));
    StreamRecord streamRecord = new StreamRecord();
    streamRecord.setNewImage(finalMap);

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
