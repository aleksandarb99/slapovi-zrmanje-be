package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.shared.model.Accommodation;
import com.slapovizrmanje.shared.model.Guests;
import com.slapovizrmanje.shared.model.enums.AccommodationState;
import com.slapovizrmanje.shared.model.enums.AccommodationType;
import com.slapovizrmanje.shared.model.enums.Language;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
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
    private static Translator translator;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String LI_TEMPLATE = "<li>%s: %d</li>";

    public Function<SQSEvent, SQSEvent> sendEmailNotification() {
        return emailNotificationsEvent -> {
            emailNotificationsEvent.getRecords().forEach(record -> {
                try {
                    final Accommodation accommodation = objectMapper.readValue(record.getBody(), Accommodation.class);
                    log.info(String.format("Received accommodation: %s", accommodation));
                    translator = choseTranslator(accommodation.getLanguage());
                    if (accommodation.getState().equals(AccommodationState.EMAIL_NOT_VERIFIED)) {
                        sendVerificationEmail(accommodation);
                    } else {
                        log.info(String.format("Email type - %s not handled yet!", accommodation.getType()));
                    }
                } catch (final JsonProcessingException e) {
                    log.error("Unexpected accommodation type", e);
                }
            });
            return emailNotificationsEvent;
        };
    }

    private void sendVerificationEmail(final Accommodation accommodation) {
        String emailBody = "";
        try {
            final String verificationLink = String.format("%s/verify?email=%s&id=%s&code=%s",
                    url, accommodation.getEmail(), accommodation.getId(), accommodation.getCode());
            final String infoParagraph = generateInfoParagraph(accommodation);
            final File resource = ResourceUtils.getFile("classpath:email-template.html");
            final String template = new String(Files.readAllBytes(resource.toPath()));
            final String verifyText = String.format(translator.getVerifyText(), translator.getAccommodation(accommodation.getType()));
            emailBody = String.format(template, verifyText, translator.getHello(), infoParagraph, verificationLink, translator.getVerifyButton(), translator.getBye());
        } catch (final IOException e) {
            log.error(String.format("Error sending email notification...\nError message: %s", e.getMessage()));
        }

        Body bodyContent = new Body().withHtml(new Content().withCharset(UTF_8).withData(emailBody));
        Message message = new Message()
                .withBody(bodyContent)
                .withSubject(new Content().withCharset(UTF_8).withData(String.format("Slapovi Zrmanje %s", translator.getNotification())));

//        TODO: Ovde ce biti s sajta naseg, tj domena
        String source = "jovansimic995@gmail.com";
        Destination destination = new Destination().withToAddresses(accommodation.getEmail());

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

    private String generateInfoParagraph(final Accommodation accommodation) {
        return generateVerificationText(accommodation);
//        return switch (accommodation.get()) {
//            case CAMP -> generateVerificationText(accommodation);
//            case CAMP_VERIFY ->
//                    String.format("Rok za uplatu u grupi <b>%s</b> se bli&#382;i - <b>%s</b>. Info za uplatu mo&#382;e&#353; videti u aplikaciji.",
//                            notification.getGroupName(), notification.getDeadline());
//            case CAMP_VERIFY -> null;
//        };
    }

    private String generateVerificationText(final Accommodation accommodation) {
        Guests guests = accommodation.getGuests();
        String guestsString = translator.getGuests() + ":<ul>" +
                (guests.getAdults() > 0 ? String.format(LI_TEMPLATE, translator.getAdults(), guests.getAdults()) : "") +
                (guests.getChildren() > 0 ? String.format(LI_TEMPLATE, translator.getChildren(), guests.getChildren()) : "") +
                (guests.getInfants() > 0 ? String.format(LI_TEMPLATE, translator.getInfants(), guests.getInfants()) : "") +
                (guests.getPets() > 0 ? String.format(LI_TEMPLATE, translator.getPets(), guests.getPets()) : "") +
                "</ul>";
        StringBuilder lodgingBuilder = new StringBuilder(translator.getLodging());
        lodgingBuilder.append(":<ul>");
        accommodation.getLodging().forEach((key, value) -> {
            if (value > 0) {
                lodgingBuilder.append(String.format(LI_TEMPLATE, translator.getLodgingType(key), value));
            }
        });
        lodgingBuilder.append("</ul>");
        String infoTemplate = "%s: %s<br>" +    // First Name
                "%s: %s<br>" +                  // Last Name
                "%s: %s<br>" +                  // Check-In
                "%s: %s<br><br>" +              // Check-Out
                "%s" +                          // Guests
                "%s" +                          // Lodging
                "%s";                       // Power supply (In case of CAMP Accommodation)
        String powerSupply = accommodation.getType().equals(AccommodationType.CAMP) ?
                String.format("%s: %s", translator.getPowerSupply(), accommodation.isPowerSupply() ? translator.getYes() : translator.getNo()) :
                "";
        return String.format(infoTemplate,
                translator.getFirstName(),
                accommodation.getFirstName(),
                translator.getLastName(),
                accommodation.getLastName(),
                translator.getCheckIn(),
                formatter.format(accommodation.getStartDate()),
                translator.getCheckOut(),
                formatter.format(accommodation.getEndDate()),
                guestsString,
                lodgingBuilder,
                powerSupply
                );
    }

    private Translator choseTranslator(Language language) {
        switch (language) {
            case HR -> {
                return Translator.croatianTranslations;
            }
            case DE -> {
                return Translator.germanTranslations;
            }
            default -> {
                return Translator.englishTranslations;
            }
        }
    }

    //        StringBuilder lodgingBuilder = new StringBuilder("Lodging:<ul>");
//        accommodation.getLodging().forEach((key, value) -> {
//            if (value > 0) {
//                lodgingBuilder.append("<li>");
//                lodgingBuilder.append(key.substring(0,1).toUpperCase());
//                lodgingBuilder.append(!key.equals("sleeping_bag") ? key.substring(1) : Arrays.toString(key.substring(1).split("_")));
//                lodgingBuilder.append(": ");
//                lodgingBuilder.append(value);
//                lodgingBuilder.append("</li>");
//            }
//        });
//        lodgingBuilder.append("</ul>");
}
