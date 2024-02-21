package com.slapovizrmanje.functions;

import com.slapovizrmanje.shared.model.enums.AccommodationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Translator {
    private String hello;
    private String bye;
    private String verifyButton;
    private String verifyText;
    private String notification;
    @Getter(AccessLevel.PRIVATE)
    private String camp;
    @Getter(AccessLevel.PRIVATE)
    private String apartment;
    @Getter(AccessLevel.PRIVATE)
    private String room;
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
            .bye("Best regards")
            .verifyButton("Verify request")
            .verifyText("Verify your %s request!")
            .notification("Notification")
            .camp("camp")
            .apartment("apartment")
            .room("room")
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
            .bye("Srda&#269;an pozdrav")
            .verifyButton("Potvrdi zahtev")
            .verifyText("Potvrdite svoj zahtev za %s!")
            .notification("Obavestenje")
            .camp("kamp")
            .apartment("apartman")
            .room("sobu")
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
        // TODO Add more room_1, room_2, apartment_1, apartment_2 etc
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
            case "sleeping_bag" -> {
                return getSleepingBag();
            }
            default -> {
                return "TODO Change code";
            }
        }
    }
}
