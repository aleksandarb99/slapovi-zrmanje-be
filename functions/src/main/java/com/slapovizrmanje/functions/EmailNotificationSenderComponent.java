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
    private static Translator translator;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String LI_TEMPLATE = "<li>%s: %d</li>";
    private static final String UTF_8 = "UTF-8";
    // TODO: Ovde ce biti s sajta naseg, tj domena
    private static final String SOURCE_EMAIL = "jovansimic995@gmail.com";

    public Function<SQSEvent, SQSEvent> sendEmailNotification() {
        return emailNotificationsEvent -> {
            emailNotificationsEvent.getRecords().forEach(record -> {
                try {
                    final Accommodation accommodation = objectMapper.readValue(record.getBody(), Accommodation.class);
                    log.info(String.format("Received accommodation: %s", accommodation));
                    translator = chooseTranslator(accommodation.getLanguage());
                    if (accommodation.getState().equals(AccommodationState.EMAIL_NOT_VERIFIED)) {
                        sendVerificationEmail(accommodation);
                    } else if(accommodation.getState().equals(AccommodationState.EMAIL_VERIFIED)) {
//                        sendVerificationConfirmEmail(accommodation);
                        sendAccommodationRequestEmail(accommodation);
                    } else if(accommodation.getState().equals(AccommodationState.NOT_AVAILABLE)) {
                        sendRejectedAccommodationRequestEmail(accommodation);
                    } else if(accommodation.getState().equals(AccommodationState.AVAILABLE)) {
                        sendAcceptedAccommodationRequestEmail(accommodation);
                    } else {
                        log.info(String.format("Email type - %s not handled yet!", accommodation.getType()));
                    }
                } catch (final JsonProcessingException e) {
                    log.error("Unexpected accommodation type", e);
                } catch (final IOException e) {
                    log.error(String.format("Error sending email notification...Error message: %s", e.getMessage()));
                }
            });
            return emailNotificationsEvent;
        };
    }

    private void sendVerificationEmail(final Accommodation accommodation) throws IOException {
        final String template = readTemplate("verification-request-email-template.html");
        final String verificationHeader = String.format(translator.getVerifyText(), translator.getAccommodation(accommodation.getType()));
        final String accommodationSummary = generateAccommodationSummaryText(accommodation);
        final String verificationLink = String.format("%s/verify?email=%s&id=%s&code=%s", url, accommodation.getEmail(), accommodation.getId(), accommodation.getCode());
        final String emailBody = String.format(template, verificationHeader, translator.getHello(), accommodationSummary, verificationLink, translator.getVerifyButton(), translator.getBye());

        sendEmail(accommodation.getEmail(), emailBody);
    }

    private void sendVerificationConfirmEmail(final Accommodation accommodation) throws IOException {
        final String template = readTemplate("verification-confirm-email-template.html");
        final String verificationConfirmHeader = String.format(translator.getVerifyConfirmText(), translator.getAccommodation(accommodation.getType()));
        final String verificationConfirmText = generateVerificationConfirmText(accommodation);
        final String emailBody = String.format(template, verificationConfirmHeader, translator.getHello(), verificationConfirmText, translator.getBye());

        sendEmail(accommodation.getEmail(), emailBody);
    }

    private void sendAccommodationRequestEmail(final Accommodation accommodation) throws IOException {
        translator = Translator.croatianTranslations;
        final String template = readTemplate("accommodation-request-email-template.html");
        final String accommodationType = translator.getAccommodation(accommodation.getType());
        final String accommodationSummary = generateAccommodationSummaryText(accommodation);
        final String rejectionLink = String.format("%s/reject?email=%s&id=%s&code=%s", url, accommodation.getEmail(), accommodation.getId(), accommodation.getCode());
        final String acceptanceLink = String.format("%s/accept?email=%s&id=%s&code=%s", url, accommodation.getEmail(), accommodation.getId(), accommodation.getCode());
        final String emailBody = String.format(template, accommodationType, accommodationSummary, rejectionLink, acceptanceLink);

        sendEmail(accommodation.getEmail(), emailBody);
    }

    private void sendRejectedAccommodationRequestEmail(Accommodation accommodation) {
//        TODO: Send email here
    }

    private void sendAcceptedAccommodationRequestEmail(Accommodation accommodation) {
        //        TODO: Send email here
    }

    private String generateAccommodationSummaryText(final Accommodation accommodation) {
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

    private String generateVerificationConfirmText(final Accommodation accommodation) {
        return String.format(translator.getVerifyConfirmParagraph(), translator.getAccommodation(accommodation.getType()), accommodation.getId());
    }

    private void sendEmail(String destinationEmailAddress, String emailBody) {
        Body bodyContent = new Body().withHtml(new Content().withCharset(UTF_8).withData(emailBody));
        Message message = new Message()
                .withBody(bodyContent)
                .withSubject(new Content().withCharset(UTF_8).withData(String.format("Slapovi Zrmanje %s", translator.getNotification())));
        Destination destination = new Destination().withToAddresses(destinationEmailAddress);

        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withSource(SOURCE_EMAIL)
                    .withDestination(destination)
                    .withMessage(message);
            log.info("Sending an email via SES.");
            sesClient.sendEmail(request);
            log.info("Email has been sent.");
        } catch (final Exception e) {
            log.error(String.format("Error sending email notification...\nError message: %s", e.getMessage()));
        }
    }

    private Translator chooseTranslator(Language language) {
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

    private String readTemplate(String templateName) throws IOException {
        final String templatePath = String.format("classpath:%s", templateName);
        final File resource = ResourceUtils.getFile(templatePath);
        return new String(Files.readAllBytes(resource.toPath()));
    }
}
