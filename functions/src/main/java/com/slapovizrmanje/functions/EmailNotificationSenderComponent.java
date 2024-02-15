package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSenderComponent {

    private final AmazonSimpleEmailService sesClient;
    private final ObjectMapper objectMapper;

    public Function<SQSEvent, SQSEvent> sendEmailNotification() {
        return emailNotificationsEvent -> {
            emailNotificationsEvent.getRecords().forEach(record -> {
                try {
                    final Notification notification = objectMapper.readValue(record.getBody(), Notification.class);
                    switch (notification.getEmailType()) {
                        case "CAMP_REQUEST" -> sendEmail(notification);
//                        case "ROOM_REQUEST" -> sendEmail(notification);
//                        case "APARTMENT_REQUEST" -> sendEmail(notification);
                        default -> log.info(String.format("Email type - %s not handled yet!", notification.getEmailType()));
                    }
                } catch (final JsonProcessingException e) {
                    log.error("Unexpected notification type", e);
                }
            });
            return emailNotificationsEvent;
        };
    }

    private void sendEmail(final Notification notification) {
        log.info("Received notification: " + notification);

        Destination destination = new Destination().withToAddresses(notification.getEmailAddress());

        // TODO Consider also responding based on preferable language
        // TODO Maybe also upgrade HTML
        String bodyHTML = String.format(
                "<html><head></head><body><h1>Hello!</h1><p> Confirm your camp reservation: %s%s.</p></body></html>",
                "https://dvwuhobnm2.execute-api.eu-central-1.amazonaws.com/prod/api/reservation/confirm/camp/",
                notification.getRecordId());

        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(destination)
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(bodyHTML)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData("Confirm camp request")))
                    .withSource("jovansimic995@gmail.com");
            log.info("Sending an email via SES.");
            sesClient.sendEmail(request);
            log.info("Email has been sent.");
        } catch (final Exception e) {
            log.error("Error sending email notification...", e);

        }
    }
}
