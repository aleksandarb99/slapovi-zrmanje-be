package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.shared.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSenderComponent {

//    TODO: Env; Frontend url ce biti
    private final String url = "https://d3gxkr4tgt0zlg.cloudfront.net";
    private final AmazonSimpleEmailService sesClient;
    private final ObjectMapper objectMapper;
    private static final String UTF_8 = "UTF-8";

    public Function<SQSEvent, SQSEvent> sendEmailNotification() {
        return emailNotificationsEvent -> {
            emailNotificationsEvent.getRecords().forEach(record -> {
                try {
                    final Notification notification = objectMapper.readValue(record.getBody(), Notification.class);
                    switch (notification.getType()) {
                        case CAMP_REQUEST -> sendEmail(notification);
//                        case "ROOM_REQUEST" -> sendEmail(notification);
//                        case "APARTMENT_REQUEST" -> sendEmail(notification);
                        default -> log.info(String.format("Email type - %s not handled yet!", notification.getType()));
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
        // TODO Upgrade HTML with additional info in Notification
        String verificationText = String.format(
                "Verify your camp reservation: %s/api/verification/verify?email=%s&id=%s.",
                url,
                notification.getEmail(),
                notification.getRecordId());
        String verificationHTML = String.format(
                "<html><head></head><body><h1>Verify submitted camp request</h1>" +
                        "<p> Start date: %s</p>" +
                        "<p> End date: %s</p>" +
                        "<p> %s</p>" +
                        "</body></html>",
                notification.getStartDate(),
                notification.getEndDate(),
                verificationText);

        Body bodyContent = new Body()
                .withHtml(new Content().withCharset(UTF_8).withData(verificationHTML))
                .withText(new Content().withCharset(UTF_8).withData(verificationText));
        Message message = new Message()
                .withBody(bodyContent)
                .withSubject(new Content().withCharset(UTF_8).withData("Verify camp reservation request"));

//        TODO: Ovde ce biti s sajta naseg, tj domena
        String source = "jovansimic995@gmail.com";
        Destination destination = new Destination().withToAddresses(notification.getEmail());

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
