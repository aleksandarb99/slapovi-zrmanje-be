package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@Slf4j
public class EmailNotificationSenderComponent {

    private final ObjectMapper objectMapper;

    public EmailNotificationSenderComponent(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Function<SQSEvent, SQSEvent> sendEmailNotification() {
        return emailNotificationsEvent -> {
            emailNotificationsEvent.getRecords().forEach(record -> {
                try {
                    final Notification notification = objectMapper.readValue(record.getBody(), Notification.class);
                    sendEmail(notification);
                } catch (final JsonProcessingException e) {
                    log.error("Unexpected notification type", e);
                }
            });
            return emailNotificationsEvent;
        };
    }

    private void sendEmail(final Notification notification) {
        try {
            log.info("Received notification: " + notification);
        } catch (final Exception e) {
            log.error("Error sending email notification...", e);
        }
    }
}
