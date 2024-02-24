package com.slapovizrmanje.api.service;

import com.slapovizrmanje.api.dao.AccommodationDao;
import com.slapovizrmanje.api.exception.NotFoundException;
import com.slapovizrmanje.api.util.Prices;
import com.slapovizrmanje.shared.dto.AccommodationRequestDTO;
import com.slapovizrmanje.api.util.TimeProvider;
import com.slapovizrmanje.api.util.Validator;
import com.slapovizrmanje.api.exception.BadRequestException;
import com.slapovizrmanje.shared.dto.PriceItemDTO;
import com.slapovizrmanje.shared.dto.PriceResponseDTO;
import com.slapovizrmanje.shared.mapper.AccommodationMapper;
import com.slapovizrmanje.shared.model.Accommodation;
import com.slapovizrmanje.shared.model.enums.AccommodationState;
import com.slapovizrmanje.shared.model.enums.AccommodationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationService {
  private final AccommodationMapper accommodationMapper;
  private final AccommodationDao accommodationDao;
  private final String priceItemTemplate = "%s  X %s    X %s nights";

  public PriceResponseDTO checkPrice(AccommodationRequestDTO accommodationRequestDTO) {
//    TODO: Validiraj


    PriceResponseDTO dto = PriceResponseDTO.builder()
            .priceItems(new ArrayList<>())
            .build();

    double totalPrice = 0;
    if (accommodationRequestDTO.getType().equals(AccommodationType.CAMP)) {
      long numberOfNights = DAYS.between(accommodationRequestDTO.getStartDate(), accommodationRequestDTO.getEndDate());
      log.info(String.format("ACCOMMODATION - Number of nights is %s.", numberOfNights));

      int adults = accommodationRequestDTO.getGuests().getAdults();
      if (adults != 0) {
        double price = Prices.adultsPrice * numberOfNights * adults;
        PriceItemDTO itemDTO = PriceItemDTO.builder()
                .name(String.format(priceItemTemplate, getItemName("jjjj"), adults, numberOfNights))
                .price(price)
                .build();

        totalPrice += price;
        dto.getPriceItems().add(itemDTO);
      }
      int children = accommodationRequestDTO.getGuests().getChildren();
      if (children != 0) {
        double price = Prices.childrenPrice * numberOfNights * children;
        PriceItemDTO itemDTO = PriceItemDTO.builder()
                .name(String.format(priceItemTemplate, getItemName("Children"), children, numberOfNights))
                .price(price)
                .build();

        totalPrice += price;
        dto.getPriceItems().add(itemDTO);
      }
      int infants = accommodationRequestDTO.getGuests().getInfants();
      if (infants != 0) {
        double price = Prices.infantsPrice * numberOfNights * infants;
        PriceItemDTO itemDTO = PriceItemDTO.builder()
                .name(String.format(priceItemTemplate, getItemName("Infants"), infants, numberOfNights))
                .price(price)
                .build();

        totalPrice += price;
        dto.getPriceItems().add(itemDTO);
      }
      int pets = accommodationRequestDTO.getGuests().getPets();
      if (pets != 0) {
        double price = Prices.petsPrice * numberOfNights * pets;
        PriceItemDTO itemDTO = PriceItemDTO.builder()
                .name(String.format(priceItemTemplate, getItemName("Pet"), pets, numberOfNights))
                .price(price)
                .build();

        totalPrice += price;
        dto.getPriceItems().add(itemDTO);
      }
      int caravan = accommodationRequestDTO.getLodging().get("caravan");
      if (caravan != 0) {
        double price = Prices.caravanPrice * numberOfNights * caravan;
        PriceItemDTO itemDTO = PriceItemDTO.builder()
                .name(String.format(priceItemTemplate, getItemName("Caravan"), caravan, numberOfNights))
                .price(price)
                .build();

        totalPrice += price;
        dto.getPriceItems().add(itemDTO);
      }
      int tent = accommodationRequestDTO.getLodging().get("tent");
      if (tent != 0) {
        double price = Prices.tentPrice * numberOfNights * tent;
        PriceItemDTO itemDTO = PriceItemDTO.builder()
                .name(String.format(priceItemTemplate, getItemName("Tent"), tent, numberOfNights))
                .price(price)
                .build();

        totalPrice += price;
        dto.getPriceItems().add(itemDTO);
      }
      int car = accommodationRequestDTO.getLodging().get("car");
      if (car != 0) {
        double price = Prices.carPrice * numberOfNights * car;
        PriceItemDTO itemDTO = PriceItemDTO.builder()
                .name(String.format(priceItemTemplate, getItemName("Car"), car, numberOfNights))
                .price(price)
                .build();

        totalPrice += price;
        dto.getPriceItems().add(itemDTO);
      }
      int speepingBag = accommodationRequestDTO.getLodging().get("speepingBag");
      if (speepingBag != 0) {
        double price = Prices.speepingBagPrice * numberOfNights * speepingBag;
        PriceItemDTO itemDTO = PriceItemDTO.builder()
                .name(String.format(priceItemTemplate, getItemName("SleepingBag"), speepingBag, numberOfNights))
                .price(price)
                .build();

        totalPrice += price;
        dto.getPriceItems().add(itemDTO);
      }
      if (accommodationRequestDTO.isPowerSupply()) {
        double price = Prices.powerSupplyCost;
        PriceItemDTO itemDTO = PriceItemDTO.builder()
                .name(String.format(priceItemTemplate, getItemName("PowerSupply"), 1, numberOfNights))
                .price(price)
                .build();

        totalPrice += price;
        dto.getPriceItems().add(itemDTO);
      }
    }
    if (accommodationRequestDTO.getType().equals(AccommodationType.APARTMENT)) {

    }
    if (accommodationRequestDTO.getType().equals(AccommodationType.ROOM)) {

    }

//    TODO: Pripremi iteme i sortiraj ih

    dto.setTotalPrice(totalPrice);
    return dto;
  }

  private String getItemName(String name) {
    int length = name.length();
    int missingWhiteSpaceNumber = 18 - length;
    return name + " " .repeat(missingWhiteSpaceNumber);
  }
  public void checkAvailability(AccommodationRequestDTO accommodationRequestDTO) {
    Validator.validateObjectToContainAtLeastOnePositive(accommodationRequestDTO.getGuests());
    Validator.validateMapToContainAtLeastOnePositive(accommodationRequestDTO.getLodging());
    Validator.validateStartEndDate(accommodationRequestDTO.getStartDate(), accommodationRequestDTO.getEndDate());

    log.info(String.format("ACCOMMODATION MAPPER - Convert to entity %s.", accommodationRequestDTO));
    Accommodation accommodation = accommodationMapper.toEntity(accommodationRequestDTO);
    accommodation.setId(UUID.randomUUID().toString());
    accommodation.setCreatedAt(System.currentTimeMillis());
    accommodation.setState(AccommodationState.EMAIL_NOT_VERIFIED);
    accommodation.setCode(UUID.randomUUID().toString());

    log.info("ACCOMMODATION DAO - Fetching by email.");
    List<Accommodation> foundEntities = accommodationDao.findByEmail(accommodation.getEmail());
    log.info(String.format("Found entities: %s", foundEntities));

//    TODO: Sta cemo ovde
    List<Accommodation> filteredAccommodations = foundEntities
            .stream()
            .filter(ac -> ac.equals(accommodation))
            .toList();

    if (!filteredAccommodations.isEmpty()) {
      LocalDateTime currentTime = LocalDateTime.now();
      filteredAccommodations
              .stream()
              .map(fac -> TimeProvider.toLocalDateTime(fac.getCreatedAt()))
              .filter(filteredTime -> currentTime.minusMinutes(15).isBefore(filteredTime))
              .findAny()
              .ifPresent(s -> {
                throw new BadRequestException("You have already raised a request.");
              });
    } else {
      log.info("There's no similar accommodation recorded.");
    }

    log.info(String.format("ACCOMMODATION DAO - Create accommodation: %s.", accommodation));
    accommodationDao.create(accommodation);
    log.info("Successfully created!");
  }

  public void verifyEmail(String email, String id, String code) {
    log.info("ACCOMMODATION DAO - Fetching by email and id pair.");
    List<Accommodation> foundEntities = accommodationDao.findByEmailAndIdPair(email,  id);
    log.info(String.format("Found entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with id '%s' does not exist.", id));
    }

    Accommodation accommodation = foundEntities.get(0);

    if (!accommodation.getState().equals(AccommodationState.EMAIL_NOT_VERIFIED)) {
      throw new BadRequestException(String.format("Entity with id '%s' is in invalid state.", id));
    }

    if (!accommodation.getCode().equals(code)) {
      throw new BadRequestException(String.format("Entity with id '%s' has different code.", id));
    }

    accommodation.setState(AccommodationState.EMAIL_VERIFIED);
    accommodation.setCode(UUID.randomUUID().toString());

    log.info(String.format("ACCOMMODATION DAO - Verify accommodation: %s.", accommodation));
    accommodationDao.update(accommodation);
    log.info("Successfully verified!");
  }

//  TODO: Kad se radi reject, mozda da se ispisu termini kad moze?
  public void reject(String email, String id, String code) {
    log.info("ACCOMMODATION DAO - Fetching by email and id pair.");
    List<Accommodation> foundEntities = accommodationDao.findByEmailAndIdPair(email,  id);
    log.info(String.format("Found entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with id '%s' does not exist.", id));
    }

    Accommodation accommodation = foundEntities.get(0);

    if (!accommodation.getState().equals(AccommodationState.EMAIL_VERIFIED)) {
      throw new BadRequestException(String.format("Entity with id '%s' is in invalid state.", id));
    }

    if (!accommodation.getCode().equals(code)) {
      throw new BadRequestException(String.format("Entity with id '%s' has different code.", id));
    }

    accommodation.setState(AccommodationState.NOT_AVAILABLE);
    accommodation.setCode(UUID.randomUUID().toString());

    log.info(String.format("ACCOMMODATION DAO - Reject accommodation: %s.", accommodation));
    accommodationDao.update(accommodation);
    log.info("Successfully rejected!");
  }

  public void accept(String email, String id, String code) {
    log.info("ACCOMMODATION DAO - Fetching by email and id pair.");
    List<Accommodation> foundEntities = accommodationDao.findByEmailAndIdPair(email,  id);
    log.info(String.format("Found entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with id '%s' does not exist.", id));
    }

    Accommodation accommodation = foundEntities.get(0);

    if (!accommodation.getState().equals(AccommodationState.EMAIL_VERIFIED)) {
      throw new BadRequestException(String.format("Entity with id '%s' is in invalid state.", id));
    }

    if (!accommodation.getCode().equals(code)) {
      throw new BadRequestException(String.format("Entity with id '%s' has different code.", id));
    }

    accommodation.setState(AccommodationState.AVAILABLE);
    accommodation.setCode(UUID.randomUUID().toString());

    log.info(String.format("ACCOMMODATION DAO - Accept accommodation: %s.", accommodation));
    accommodationDao.update(accommodation);
    log.info("Successfully accepted!");
  }

  public void reserve(String email, String id, String code) {
    log.info("ACCOMMODATION DAO - Fetching by email and id pair.");
    List<Accommodation> foundEntities = accommodationDao.findByEmailAndIdPair(email,  id);
    log.info(String.format("Found entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with id '%s' does not exist.", id));
    }

    Accommodation accommodation = foundEntities.get(0);

    if (!accommodation.getState().equals(AccommodationState.AVAILABLE)) {
      throw new BadRequestException(String.format("Entity with id '%s' is in invalid state.", id));
    }

    if (!accommodation.getCode().equals(code)) {
      throw new BadRequestException(String.format("Entity with id '%s' has different code.", id));
    }

//    TODO: Proveri da li je buducnost

    accommodation.setState(AccommodationState.RESERVED);
    accommodation.setCode(UUID.randomUUID().toString());

    log.info(String.format("ACCOMMODATION DAO - Reserved accommodation: %s.", accommodation));
    accommodationDao.update(accommodation);
    log.info("Successfully reserved!");
  }

  public void cancel(String email, String id, String code) {
    log.info("ACCOMMODATION DAO - Fetching by email and id pair.");
    List<Accommodation> foundEntities = accommodationDao.findByEmailAndIdPair(email,  id);
    log.info(String.format("Found entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with id '%s' does not exist.", id));
    }

    Accommodation accommodation = foundEntities.get(0);

    if (!accommodation.getState().equals(AccommodationState.RESERVED)) {
      throw new BadRequestException(String.format("Entity with id '%s' is in invalid state.", id));
    }

    if (!accommodation.getCode().equals(code)) {
      throw new BadRequestException(String.format("Entity with id '%s' has different code.", id));
    }

//    TODO: Proveri da li je buducnost
//TODO: Ne moze se otkazati ako je proslost

    accommodation.setState(AccommodationState.CANCELED);

    log.info(String.format("ACCOMMODATION DAO - Canceled accommodation: %s.", accommodation));
    accommodationDao.update(accommodation);
    log.info("Successfully canceled!");
  }

}
