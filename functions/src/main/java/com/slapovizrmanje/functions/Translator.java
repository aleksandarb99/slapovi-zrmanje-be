package com.slapovizrmanje.functions;

import com.slapovizrmanje.shared.model.enums.AccommodationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Translator {
  private String hello;
  private String reserveHello;
  private String bye;
  private String verifyButton;
  private String reserveButton;
  private String cancelButton;
  private String verifyText;
  private String reserveText;
  private String verifyConfirmText;
  private String reserveConfirmText;
  private String cancelConfirmText;
  private String rejectConfirmText;
  private String verifyConfirmParagraph;
  private String reserveConfirmParagraph;
  private String cancelConfirmParagraph;
  private String rejectConfirmParagraph;
  private String notification;
  @Getter(AccessLevel.PRIVATE)
  private String camp;
  @Getter(AccessLevel.PRIVATE)
  private String apartment;
  @Getter(AccessLevel.PRIVATE)
  private String room;
  @Getter(AccessLevel.PRIVATE)
  private String lodgingRoom;
  @Getter(AccessLevel.PRIVATE)
  private String lodgingApartment;
  private String firstName;
  private String lastName;
  private String checkIn;
  private String checkOut;
  private String guests;
  private String infants;
  private String adults;
  private String children;
  private String pets;
  private String lodging;
  @Getter(AccessLevel.PRIVATE)
  private String car;
  @Getter(AccessLevel.PRIVATE)
  private String caravan;
  @Getter(AccessLevel.PRIVATE)
  private String tent;
  @Getter(AccessLevel.PRIVATE)
  private String sleepingBag;
  private String powerSupply;
  private String yes;
  private String no;

  public static final Translator englishTranslations = Translator.builder()
          .hello("Hello")
          .reserveHello("Hello, your request summary once again")
          .bye("Best regards")
          .verifyButton("Verify request")
          .reserveButton("Reserve")
          .cancelButton("Cancel")
          .reserveText("Request has been accepted. Reserve your %s request now!")
          .verifyText("Verify your %s request!")
          .verifyConfirmText("Successfully confirmed %s request")
          .reserveConfirmText("Successfully reserved %s request")
          .cancelConfirmText("Successfully cancelled %s reservation")
          .rejectConfirmText("Request for %s has been rejected")
          .verifyConfirmParagraph("Your %s request number - <b>%s</b> has been successfully confirmed ✅ You can expect the feedback soon.")
          .reserveConfirmParagraph("Your %s request number - <b>%s</b> has been successfully reserved ✅ See you soon!\nIf from some reason you are not able to come, please inform us by cancelling your reservation on bellow button.")
          .cancelConfirmParagraph("Your %s reservation number - <b>%s</b> has been successfully cancelled ✅ Hope we see you next time.")
          .rejectConfirmParagraph("Your %s request number - <b>%s</b> has been rejected. Probably the chosen days are not available. You can expect in late evening an email with suggested available periods. Thank you for your patience!")
          .notification("Notification")
          .camp("camp")
          .apartment("apartment")
          .lodgingApartment("Apartment")
          .room("room")
          .lodgingRoom("Room")
          .checkIn("Check-in date")
          .checkOut("Check-out date")
          .firstName("First Name")
          .lastName("Last Name")
          .guests("Guests")
          .infants("Infants")
          .adults("Adults")
          .children("Children")
          .pets("Pets")
          .lodging("Lodging")
          .car("Car")
          .caravan("Caravan")
          .tent("Tent")
          .sleepingBag("Sleeping bag")
          .powerSupply("Power supply")
          .yes("Yes")
          .no("No")
          .build();

  public static final Translator croatianTranslations = Translator.builder()
          .hello("Zdravo")
          .reserveHello("Zdravo, rezime vaseg zahteva jos jednom")
          .bye("Srda&#269;an pozdrav")
          .verifyButton("Potvrdi zahtev")
          .reserveButton("Rezervisi")
          .cancelButton("Otkazi")
          .reserveText("Zahtev je prihvacen. Rezervisite svoj zahtev za %s sada!")
          .verifyText("Potvrdite svoj zahtev za %s!")
          .verifyConfirmText("Uspešno potvrdjen zahtev za %s!")
          .reserveConfirmText("Uspešno reservisan zahtev za %s!")
          .cancelConfirmText("Uspešno otkazana rezervacija za %s!")
          .rejectConfirmText("Zahtev za %s je odbijen")
          .verifyConfirmParagraph("Vas zahtev za %s broj - <b>%s</b> je uspesno potvrdjen ✅ Mozete ocekivati odgovor uskoro.")
          .reserveConfirmParagraph("Vas zahtev za %s broj - <b>%s</b> je uspesno reservisan ✅ Vidimo se uskoro!\n Ako iz nekog razloga niste u mogucnosti da dodjete, molimo Vas da nas obavestite, tako sto cete otkazati rezervaciju pritiskom na dugme ispod.")
          .cancelConfirmParagraph("Vasa rezervacija za %s broj - <b>%s</b> je uspesno otkazana ✅ Nadamo se da se vidimo nekom drugom prilikom.")
          .rejectConfirmParagraph("Vas zahtev za %s broj - <b>%s</b> je odbijen. Najverovatnije izabrani dani nisu dostupni. Mozete ocekivati kasno uvece email sa predlozenim slobodnim terminima. Hvala na razumevanju!")
          .notification("Obavestenje")
          .camp("kamp")
          .apartment("apartman")
          .lodgingApartment("Apartman")
          .room("sobu")
          .lodgingRoom("Soba")
          .checkIn("Datum prijave")
          .checkOut("Datum odjave")
          .firstName("Ime")
          .lastName("Prezime")
          .guests("Gosti")
          .infants("Odoj&#269;adi")
          .adults("Odrasli")
          .children("Deca")
          .pets("Ljubimci")
          .lodging("Preno&#263;iste")
          .car("Automobil")
          .caravan("Karavan")
          .tent("&#352;ator")
          .sleepingBag("Vre&#263;a za spavanje")
          .powerSupply("Snadbevanje strujom")
          .yes("Da")
          .no("Ne")
          .build();

  public static final Translator germanTranslations = Translator.builder()
          .hello("Hallo")
          .bye("Beste grüße")
          .verifyButton("Anfrage überprüfen")
          .verifyText("Bestätigen Sie Ihre %s!")
          .notification("Benachrichtigung")
          .camp("Camp-Anfrage")
          .apartment("Wohnungsanfrage")
          .room("Zimmeranfrage")
          .build();

  public String getAccommodation(AccommodationType type) {
    switch (type) {
      case ROOM -> {
        return getRoom();
      }
      case CAMP -> {
        return getCamp();
      }
      default -> {
        return getApartment();
      }
    }
  }

  public String getLodgingType(String lodgingKeyFromMap) {
    switch (lodgingKeyFromMap) {
      case "car" -> {
        return getCar();
      }
      case "caravan" -> {
        return getCaravan();
      }
      case "tent" -> {
        return getTent();
      }
      case "sleepingBag" -> {
        return getSleepingBag();
      }
      case "room1" -> {
        return String.format("%s 1", getLodgingRoom());
      }
      case "room2" -> {
        return String.format("%s 2", getLodgingRoom());
      }
      case "room3" -> {
        return String.format("%s 3", getLodgingRoom());
      }
      case "apartment1" -> {
        return String.format("%s 1", getLodgingApartment());
      }
      case "apartment2" -> {
        return String.format("%s 2", getLodgingApartment());
      }
      case "apartment3" -> {
        return String.format("%s 3", getLodgingApartment());
      }
      default -> {
        return "TODO Model has been changed. Add more mappings!";
      }
    }
  }
}
