package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.shared.dto.ContactQuestionDTO;
import com.slapovizrmanje.shared.model.Accommodation;
import com.slapovizrmanje.shared.model.Guests;
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
import java.util.List;
import java.util.Map;
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
  //    TODO: Dodaj u mejl za potvrdu rezervacije da mora bar 3 dana ranije da otkaze rezervaciju
//    TODO: Tatin mejl mora biti hardcodovan
  private static final String SOURCE_EMAIL2 = "abuljevic8@gmail.com";

  public Function<SQSEvent, SQSEvent> sendEmailNotification() {
    return emailNotificationsEvent -> {
      emailNotificationsEvent.getRecords().forEach(record -> {

        String recordBody = record.getBody();
        Map<String, SQSEvent.MessageAttribute> messageAttributeMap = record.getMessageAttributes();
        SQSEvent.MessageAttribute classMessageAttribute = messageAttributeMap.get("class");

        try {
          switch (classMessageAttribute.getStringValue()) {
            case "Accommodation" -> processAccommodationSqsEvent(recordBody);
            case "ContactQuestionDTO" -> processContactQuestionSqsEvent(recordBody);
            default -> log.info("Message attribute 'class' not setup!");
          }
        } catch (final IOException e) {
          log.error(String.format("Error sending email notification...Error message: %s", e.getMessage()));
        }

      });
      return emailNotificationsEvent;
    };
  }

  private void processContactQuestionSqsEvent(String recordBody) throws IOException {
    final ContactQuestionDTO contactQuestion = objectMapper.readValue(recordBody, ContactQuestionDTO.class);
    log.info(String.format("Received contact question: %s", contactQuestion));
    translator = chooseTranslator(Language.HR);
    final String template = readTemplate("get-in-touch-email-template.html");
    final String emailBody = String.format(template, contactQuestion.getName(), contactQuestion.getEmail(), contactQuestion.getMessage());

    sendEmail(SOURCE_EMAIL, contactQuestion.getEmail(), emailBody);
  }

  private void processAccommodationSqsEvent(String recordBody) throws IOException {
    final Accommodation accommodation = objectMapper.readValue(recordBody, Accommodation.class);
    log.info(String.format("Received accommodation: %s", accommodation));
    translator = chooseTranslator(accommodation.getLanguage());

    switch (accommodation.getState()) {
      case EMAIL_NOT_VERIFIED -> sendVerificationEmail(accommodation);
      case EMAIL_VERIFIED -> {
        sendVerificationConfirmEmail(accommodation);
        sendAccommodationRequestEmail(accommodation);
      }
      case NOT_AVAILABLE -> sendRejectedAccommodationRequestEmail(accommodation);
      case AVAILABLE -> sendAcceptedAccommodationRequestEmail(accommodation);
      case RESERVED -> {
        sendReservedConfirmEmail(accommodation);
        sendReservedAccommodationRequestEmail(accommodation);
      }
      case CANCELED -> {
        sendCanceledConfirmEmail(accommodation);
        sendCanceledAccommodationReservationEmail(accommodation);
      }
      default -> log.info(String.format("Email type - %s not handled yet!", accommodation.getType()));
    }
  }

  private void sendVerificationEmail(final Accommodation accommodation) throws IOException {
    final String template = readTemplate("one-button-email-template.html");
    final String verificationHeader = String.format(translator.getVerifyText(), translator.getAccommodation(accommodation.getType()));
    final String accommodationSummary = generateAccommodationSummaryText(accommodation);
    final String verificationLink = String.format("%s/verify?email=%s&id=%s&code=%s", url, accommodation.getEmail(), accommodation.getId(), accommodation.getCode());
    final String emailBody = String.format(template, verificationHeader, translator.getHello(), accommodationSummary, verificationLink, translator.getVerifyButton(), translator.getBye());

    sendEmail(accommodation.getEmail(), null, emailBody);
  }

  private void sendVerificationConfirmEmail(final Accommodation accommodation) throws IOException {
    final String template = readTemplate("no-buttons-email-template.html");
    final String verificationConfirmHeader = String.format(translator.getVerifyConfirmText(), translator.getAccommodation(accommodation.getType()));
    final String verificationConfirmText = generateVerificationConfirmText(accommodation);
    final String emailBody = String.format(template, verificationConfirmHeader, translator.getHello(), verificationConfirmText, translator.getBye());

    sendEmail(accommodation.getEmail(), null, emailBody);
  }

  private void sendAccommodationRequestEmail(final Accommodation accommodation) throws IOException {
    translator = Translator.croatianTranslations;
    final String template = readTemplate("two-buttons-email-template.html");
    final String accommodationType = translator.getAccommodation(accommodation.getType());
    final String accommodationSummary = generateAccommodationSummaryText(accommodation);
    final String rejectionLink = String.format("%s/reject?email=%s&id=%s&code=%s", url, accommodation.getEmail(), accommodation.getId(), accommodation.getCode());
    final String acceptanceLink = String.format("%s/accept?email=%s&id=%s&code=%s", url, accommodation.getEmail(), accommodation.getId(), accommodation.getCode());
    final String emailBody = String.format(template, accommodationType, accommodationSummary, rejectionLink, acceptanceLink);

    sendEmail(SOURCE_EMAIL2, null, emailBody);
  }

  private void sendRejectedAccommodationRequestEmail(Accommodation accommodation) throws IOException {
    final String template = readTemplate("no-buttons-email-template.html");
    final String rejectionHeader = String.format(translator.getRejectConfirmText(), translator.getAccommodation(accommodation.getType()));
    final String rejectionConfirmText = generateRejectionConfirmText(accommodation);
    final String emailBody = String.format(template, rejectionHeader, translator.getHello(), rejectionConfirmText, translator.getBye());

    sendEmail(accommodation.getEmail(), null, emailBody);
  }

  private void sendAcceptedAccommodationRequestEmail(Accommodation accommodation) throws IOException {
    final String template = readTemplate("one-button-email-template.html");
    final String reservationHeader = String.format(translator.getReserveText(), translator.getAccommodation(accommodation.getType()));
    final String accommodationSummary = generateAccommodationSummaryText(accommodation);
    final String reservationLink = String.format("%s/reserve?email=%s&id=%s&code=%s", url, accommodation.getEmail(), accommodation.getId(), accommodation.getCode());
    final String emailBody = String.format(template, reservationHeader, translator.getReserveHello(), accommodationSummary, reservationLink, translator.getReserveButton(), translator.getBye());

    sendEmail(accommodation.getEmail(), null, emailBody);
  }

  private void sendReservedAccommodationRequestEmail(Accommodation accommodation) throws IOException {
    translator = Translator.croatianTranslations;
    final String template = readTemplate("no-buttons-email-template.html");
    final String verificationConfirmHeader = String.format("Rezervacija za %s je prihvacena", translator.getAccommodation(accommodation.getType()));
    final String accommodationSummaryText = generateAccommodationSummaryText(accommodation);
    final String emailBody = String.format(template, verificationConfirmHeader, "Zdravo Ziko, pregled rezervacije je dat u nastavku", accommodationSummaryText, translator.getBye());

    sendEmail(SOURCE_EMAIL2, null, emailBody);
  }

  private void sendReservedConfirmEmail(Accommodation accommodation) throws IOException {
    final String template = readTemplate("one-button-email-template.html");
    final String reservationConfirmHeader = String.format(translator.getReserveConfirmText(), translator.getAccommodation(accommodation.getType()));
    final String reservationConfirmText = generateReservationConfirmText(accommodation);
    final String cancellationLink = String.format("%s/cancel?email=%s&id=%s&code=%s", url, accommodation.getEmail(), accommodation.getId(), accommodation.getCode());
    final String emailBody = String.format(template, reservationConfirmHeader, translator.getHello(), reservationConfirmText, cancellationLink, translator.getCancelButton(), translator.getBye());

    sendEmail(accommodation.getEmail(), null, emailBody);
  }

  private void sendCanceledAccommodationReservationEmail(Accommodation accommodation) throws IOException {
    translator = Translator.croatianTranslations;
    final String template = readTemplate("no-buttons-email-template.html");
    final String cancellationConfirmHeader = String.format("Rezervacija za %s je otkazana", translator.getAccommodation(accommodation.getType()));
    final String accommodationSummaryText = generateAccommodationSummaryText(accommodation);
    final String emailBody = String.format(template, cancellationConfirmHeader, "Zdravo Ziko, pregled otkazane rezervacije je dat u nastavku", accommodationSummaryText, translator.getBye());

    sendEmail(SOURCE_EMAIL2, null, emailBody);
  }

  private void sendCanceledConfirmEmail(Accommodation accommodation) throws IOException {
    final String template = readTemplate("no-buttons-email-template.html");
    final String cancellationConfirmHeader = String.format(translator.getCancelConfirmText(), translator.getAccommodation(accommodation.getType()));
    final String cancellationConfirmText = generateCancellationConfirmText(accommodation);
    final String emailBody = String.format(template, cancellationConfirmHeader, translator.getHello(), cancellationConfirmText, translator.getBye());

    sendEmail(accommodation.getEmail(), null, emailBody);
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

  private String generateRejectionConfirmText(final Accommodation accommodation) {
    return String.format(translator.getRejectConfirmParagraph(), translator.getAccommodation(accommodation.getType()), accommodation.getId());
  }

  private String generateCancellationConfirmText(final Accommodation accommodation) {
    return String.format(translator.getCancelConfirmParagraph(), translator.getAccommodation(accommodation.getType()), accommodation.getId());
  }

  private String generateReservationConfirmText(final Accommodation accommodation) {
    return String.format(translator.getReserveConfirmParagraph(), translator.getAccommodation(accommodation.getType()), accommodation.getId());
  }

  private void sendEmail(String destinationEmailAddress, String replyToEmailAddress, String emailBody) {
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
      if (replyToEmailAddress != null) {
        request.setReplyToAddresses(List.of(replyToEmailAddress));
      }
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
