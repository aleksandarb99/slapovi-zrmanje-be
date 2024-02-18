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
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
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

        String emailBody = "";
        try {
            final String verificationLink = String.format(
                    "%s/api/verification/verify?email=%s&id=%s", url, notification.getEmail(), notification.getRecordId());
            final String infoParagraph = generateInfoParagraph(notification);
            final File resource = ResourceUtils.getFile("classpath:email-template.html");
            final String template = new String(Files.readAllBytes(resource.toPath()));
            emailBody = String.format(template, infoParagraph, verificationLink);
        } catch (final IOException e) {
            log.error(String.format("Error sending email notification...\nError message: %s", e.getMessage()));
        }

        Body bodyContent = new Body().withHtml(new Content().withCharset(UTF_8).withData(emailBody));
        Message message = new Message()
                .withBody(bodyContent)
                .withSubject(new Content().withCharset(UTF_8).withData("Slapovi Zrmanje Notification"));

//        TODO: Ovde ce biti s sajta naseg, tj domena
        String source = "jovansimic995@gmail.com";
        Destination destination = new Destination().withToAddresses(notification.getEmail());

        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withSource(source)
                    .withDestination(destination)
                    .withMessage(message);
            log.info("Sending an email via SES.");
            sesClient.sendEmail(request);
            log.info("Email has been sent.");
        } catch (final Exception e) {
            log.error(String.format("Error sending email notification...\nError message: %s", e.getMessage()));
        }
    }

    private String generateInfoParagraph(final Notification notification) {
        return switch (notification.getType()) {
            case CAMP_REQUEST -> generateVerificationText(notification);
//            case CAMP_VERIFY ->
//                    String.format("Rok za uplatu u grupi <b>%s</b> se bli&#382;i - <b>%s</b>. Info za uplatu mo&#382;e&#353; videti u aplikaciji.",
//                            notification.getGroupName(), notification.getDeadline());
            case CAMP_VERIFY -> null;
        };
    }

    private String generateVerificationText(final Notification notification) {
        StringBuilder guestsBuilder = new StringBuilder("<br>Guests:<ul>");
        notification.getGuests().forEach((key, value) -> {
            if (value > 0) {
                guestsBuilder.append("<li>");
                guestsBuilder.append(key.substring(0,1).toUpperCase());
                guestsBuilder.append(key, 1, key.length());
                guestsBuilder.append(": ");
                guestsBuilder.append(value);
                guestsBuilder.append("</li>");
            }
        });
        guestsBuilder.append("</ul>");
        StringBuilder lodgingBuilder = new StringBuilder("Lodging:<ul>");
        notification.getLodging().forEach((key, value) -> {
            if (value > 0) {
                lodgingBuilder.append("<li>");
                lodgingBuilder.append(key.substring(0,1).toUpperCase());
                lodgingBuilder.append(!key.equals("sleeping_bag") ? key.substring(1) : Arrays.toString(key.substring(1).split("_")));
                lodgingBuilder.append(": ");
                lodgingBuilder.append(value);
                lodgingBuilder.append("</li>");
            }
        });
        lodgingBuilder.append("</ul>");
        String text = String.format("Ime: %s<br>Prezime: %s<br>Datum prijavljivanja: %s<br>Datum odjavljivanja: %s<br>%s<br>%s<br>",
                notification.getFirstName(),
                notification.getLastName(),
                notification.getStartDate(),
                notification.getEndDate(),
                guestsBuilder,
                lodgingBuilder);
        return text;
    }
}
