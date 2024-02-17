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

        // TODO Consider also responding based on preferable language
        // TODO Maybe also upgrade HTML
        String verificationText = String.format(
                "Verify your camp reservation: %s%s.",
                "https://d3gxkr4tgt0zlg.cloudfront.net/camp/verify/",
                notification.getRecordId());
        String verificationHTML = String.format(
                "<html><head></head><body><h1>Hello!</h1><p> %s</p></body></html>",
                verificationText);

        Content textContent = new Content().withCharset("UTF-8").withData(verificationText);
        Body htmlContent = new Body().withHtml(new Content().withCharset("UTF-8").withData(verificationHTML));
        Message message = new Message()
                .withBody(htmlContent)
                .withSubject(textContent);
        String source = "jovansimic995@gmail.com";
        Destination destination = new Destination().withToAddresses(notification.getEmailAddress());

        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withSource(source)
                    .withDestination(destination)
                    .withMessage(message);
            log.info(String.format("Sending an email via SES; %s.", request));
            sesClient.sendEmail(request);
            log.info("Email has been sent.");
        } catch (final Exception e) {
            log.error(String.format("Error sending email notification...\nError message: %s", e.getMessage()));
        }
    }
}
