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
