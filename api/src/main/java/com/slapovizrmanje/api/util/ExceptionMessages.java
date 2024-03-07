package com.slapovizrmanje.api.util;

import com.slapovizrmanje.shared.model.enums.Language;

import java.util.HashMap;
import java.util.Map;

public class ExceptionMessages {

//  Key is 'Language_ExceptionType', Value is template


//       Language.EN + "_" + ExceptionMessageType.BadRequestExceptionCheckInMustBeInFutureMessage,
//        "Provided check in date has to be in future.",

  private static Map<String, String> messages;
  static {
    messages = new HashMap<>();

    //    English
    messages.put(Language.EN + "_" + ExceptionMessageType.NotFoundExceptionMessage,
            "Entity with the id '%s' does not exist.");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionInvalidStateMessage,
            "Entity with the id '%s' is in invalid state.");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionInvalidCodeMessage,
            "Entity with the id '%s' has different code.");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionCannotCancelMessage,
            "Entity with the ID '%s' cannot be canceled as its start date is within the next 3 days.");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionAlreadySentRequestMessage,
            "You have already raised a request.");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionStartDateNotInTheFutureMessage,
            "Entity with the id '%s' has start date which is not in the future.");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionBadTypeMessage,
            "Request type is not valid");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionFieldHasToBePositiveMessage,
            "Field - %s has to be a positive value.");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionOneFieldMustBePositiveMessage,
            "%s - At least one field has to be a positive value!");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionCheckOutMustBeAfterCheckInMessage,
            "Provided check out date has to be after the check in date.");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionCheckInMustBeInFutureMessage,
            "Provided check in date has to be in future.");
    messages.put(Language.EN + "_" + ExceptionMessageType.BadRequestExceptionOverCapacityMessage,
            "Request contains %s people but the combined maximum capacity of the selected accommodations is %s.");

    // German language messages
    messages.put(Language.DE + "_" + ExceptionMessageType.NotFoundExceptionMessage,
            "Entität mit der ID '%s' existiert nicht.");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionInvalidStateMessage,
            "Entität mit der ID '%s' befindet sich in einem ungültigen Zustand.");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionInvalidCodeMessage,
            "Entität mit der ID '%s' hat einen anderen Code.");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionCannotCancelMessage,
            "Die Entität mit der ID '%s' kann nicht storniert werden, da ihr Startdatum innerhalb der nächsten 3 Tage liegt.");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionAlreadySentRequestMessage,
            "Sie haben bereits eine Anfrage gestellt.");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionStartDateNotInTheFutureMessage,
            "Entität mit der ID '%s' hat ein Startdatum, das nicht in der Zukunft liegt.");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionBadTypeMessage,
            "Anforderungstyp ist ungültig");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionFieldHasToBePositiveMessage,
            "Feld - %s muss einen positiven Wert haben.");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionOneFieldMustBePositiveMessage,
            "%s - Mindestens ein Feld muss einen positiven Wert haben!");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionCheckOutMustBeAfterCheckInMessage,
            "Das angegebene Check-out-Datum muss nach dem Check-in-Datum liegen.");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionCheckInMustBeInFutureMessage,
            "Das angegebene Check-in-Datum muss in der Zukunft liegen.");
    messages.put(Language.DE + "_" + ExceptionMessageType.BadRequestExceptionOverCapacityMessage,
            "Die Anfrage enthält %s Personen, aber die maximale Kapazität der ausgewählten Unterkünfte beträgt %s.");

    // Croatian messages
    messages.put(Language.HR + "_" + ExceptionMessageType.NotFoundExceptionMessage,
            "Entitet s ID-om '%s' ne postoji.");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionInvalidStateMessage,
            "Entitet s ID-om '%s' je u nevažećem stanju.");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionInvalidStateMessage,
            "Entitet s ID-om '%s' ima drugačiji kod.");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionCannotCancelMessage,
            "Entitet s ID-om '%s' ne može se otkazati jer je njegov datum početka unutar sljedećih 3 dana.");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionAlreadySentRequestMessage,
            "Već ste podnijeli zahtjev.");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionStartDateNotInTheFutureMessage,
            "Entitet s ID-om '%s' ima datum početka koji nije u budućnosti.");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionBadTypeMessage,
            "Vrsta zahtjeva nije valjana");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionFieldHasToBePositiveMessage,
            "Polje - %s mora biti pozitivna vrijednost.");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionOneFieldMustBePositiveMessage,
            "%s - Barem jedno polje mora biti pozitivna vrijednost!");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionCheckOutMustBeAfterCheckInMessage,
            "Unešeni datum odjave mora biti nakon datuma prijave.");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionCheckInMustBeInFutureMessage,
            "Unešeni datum prijave mora biti u budućnosti.");
    messages.put(Language.HR + "_" + ExceptionMessageType.BadRequestExceptionOverCapacityMessage,
            "Zahtjev sadrži %s osoba, ali maksimalni kapacitet odabranih smještajnih jedinica je %s.");
  }
  public static String getMessage(Language language, ExceptionMessageType type) {
    return messages.get(language + "_" + type);
  }
}
