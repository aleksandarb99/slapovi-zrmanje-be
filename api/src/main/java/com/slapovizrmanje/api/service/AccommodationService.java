package com.slapovizrmanje.api.service;

import com.slapovizrmanje.api.exception.BadRequestException;
import com.slapovizrmanje.api.exception.NotFoundException;
import com.slapovizrmanje.api.util.TimeProvider;
import com.slapovizrmanje.api.util.Validator;
import com.slapovizrmanje.shared.dao.AccommodationDao;
import com.slapovizrmanje.shared.dto.AccommodationRequestDTO;
import com.slapovizrmanje.shared.mapper.AccommodationMapper;
import com.slapovizrmanje.shared.model.Accommodation;
import com.slapovizrmanje.shared.model.enums.AccommodationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationService {
  private final AccommodationMapper accommodationMapper;
  private final AccommodationDao accommodationDao;

  public void checkAvailability(AccommodationRequestDTO requestDTO) {
    Validator.validateObjectToContainAtLeastOnePositive(requestDTO.getGuests());
    Validator.validateMapToContainAtLeastOnePositive(requestDTO.getLodging());
    Validator.validateStartEndDate(requestDTO.getStartDate(), requestDTO.getEndDate());

    log.info(String.format("ACCOMMODATION MAPPER - Convert to entity %s.", requestDTO));
    Accommodation accommodation = accommodationMapper.toEntity(requestDTO);
    accommodation.setId(UUID.randomUUID().toString());
    accommodation.setCreatedAt(System.currentTimeMillis());
    accommodation.setState(AccommodationState.EMAIL_NOT_VERIFIED);
    accommodation.setCode(UUID.randomUUID().toString());
    accommodation.setLastModified(LocalDate.now());

    log.info("ACCOMMODATION DAO - Fetching by email.");
    List<Accommodation> foundEntities = accommodationDao.findByEmail(accommodation.getEmail());
    log.info(String.format("Found entities: %s", foundEntities));

    List<Accommodation> filteredAccommodations = foundEntities
            .stream()
            .filter(ac -> ac.equals(accommodation))
            .toList();

    if (!filteredAccommodations.isEmpty()) {
      LocalDateTime currentTime = LocalDateTime.now();
      filteredAccommodations = filteredAccommodations.stream().filter(fa -> fa.getType().equals(accommodation.getType())).toList();
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
    List<Accommodation> foundEntities = accommodationDao.findByEmailAndIdPair(email, id);
    log.info(String.format("Found entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with the id '%s' does not exist.", id));
    }

    Accommodation accommodation = foundEntities.get(0);

    if (!accommodation.getState().equals(AccommodationState.EMAIL_NOT_VERIFIED)) {
      throw new BadRequestException(String.format("Entity with the id '%s' is in invalid state.", id));
    }

    if (!accommodation.getCode().equals(code)) {
      throw new BadRequestException(String.format("Entity with the id '%s' has different code.", id));
    }

    accommodation.setState(AccommodationState.EMAIL_VERIFIED);
    accommodation.setCode(UUID.randomUUID().toString());
    accommodation.setLastModified(LocalDate.now());

    log.info(String.format("ACCOMMODATION DAO - Verify accommodation: %s.", accommodation));
    accommodationDao.update(accommodation);
    log.info("Successfully verified!");
  }

  public void reject(String email, String id, String code) {
    log.info("ACCOMMODATION DAO - Fetching by email and id pair.");
    List<Accommodation> foundEntities = accommodationDao.findByEmailAndIdPair(email, id);
    log.info(String.format("Found entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with the id '%s' does not exist.", id));
    }

    Accommodation accommodation = foundEntities.get(0);

    if (!accommodation.getState().equals(AccommodationState.EMAIL_VERIFIED)) {
      throw new BadRequestException(String.format("Entity with the id '%s' is in invalid state.", id));
    }

    if (!accommodation.getCode().equals(code)) {
      throw new BadRequestException(String.format("Entity with the id '%s' has different code.", id));
    }

    accommodation.setState(AccommodationState.NOT_AVAILABLE);
    accommodation.setCode(UUID.randomUUID().toString());
    accommodation.setLastModified(LocalDate.now());

    log.info(String.format("ACCOMMODATION DAO - Reject accommodation: %s.", accommodation));
    accommodationDao.update(accommodation);
    log.info("Successfully rejected!");
  }

  public void accept(String email, String id, String code) {
    log.info("ACCOMMODATION DAO - Fetching by email and id pair.");
    List<Accommodation> foundEntities = accommodationDao.findByEmailAndIdPair(email, id);
    log.info(String.format("Found entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with id the '%s' does not exist.", id));
    }

    Accommodation accommodation = foundEntities.get(0);

    if (!accommodation.getState().equals(AccommodationState.EMAIL_VERIFIED)) {
      throw new BadRequestException(String.format("Entity with the id '%s' is in invalid state.", id));
    }

    if (!accommodation.getCode().equals(code)) {
      throw new BadRequestException(String.format("Entity with the id '%s' has different code.", id));
    }

    accommodation.setState(AccommodationState.AVAILABLE);
    accommodation.setCode(UUID.randomUUID().toString());
    accommodation.setLastModified(LocalDate.now());

    log.info(String.format("ACCOMMODATION DAO - Accept accommodation: %s.", accommodation));
    accommodationDao.update(accommodation);
    log.info("Successfully accepted!");
  }

  public void reserve(String email, String id, String code) {
    log.info("ACCOMMODATION DAO - Fetching by email and id pair.");
    List<Accommodation> foundEntities = accommodationDao.findByEmailAndIdPair(email, id);
    log.info(String.format("Found entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with the id '%s' does not exist.", id));
    }

    Accommodation accommodation = foundEntities.get(0);

    if (!accommodation.getState().equals(AccommodationState.AVAILABLE)) {
      throw new BadRequestException(String.format("Entity with the id '%s' is in invalid state.", id));
    }

    if (!accommodation.getCode().equals(code)) {
      throw new BadRequestException(String.format("Entity with the id '%s' has different code.", id));
    }

    if (!accommodation.getStartDate().isAfter(LocalDate.now())) {
      throw new BadRequestException(String.format("Entity with the id '%s' has start date which is not in the future.", id));
    }

    accommodation.setState(AccommodationState.RESERVED);
    accommodation.setCode(UUID.randomUUID().toString());
    accommodation.setLastModified(LocalDate.now());

    log.info(String.format("ACCOMMODATION DAO - Reserved accommodation: %s.", accommodation));
    accommodationDao.update(accommodation);
    log.info("Successfully reserved!");
  }

  public void cancel(String email, String id, String code) {
    log.info("ACCOMMODATION DAO - Fetching by email and id pair.");
    List<Accommodation> foundEntities = accommodationDao.findByEmailAndIdPair(email, id);
    log.info(String.format("Found entities: %s", foundEntities));

    if (foundEntities.isEmpty()) {
      throw new NotFoundException(String.format("Entity with the id '%s' does not exist.", id));
    }

    Accommodation accommodation = foundEntities.get(0);

    if (!accommodation.getState().equals(AccommodationState.RESERVED)) {
      throw new BadRequestException(String.format("Entity with the id '%s' is in invalid state.", id));
    }

    if (!accommodation.getCode().equals(code)) {
      throw new BadRequestException(String.format("Entity with the id '%s' has different code.", id));
    }

    if (!accommodation.getStartDate().minus(3, ChronoUnit.DAYS).isAfter(LocalDate.now())) {
      throw new BadRequestException(String.format("Entity with the id '%s' has a start date that is less than 3 days in the future from now.", id));
    }

    accommodation.setState(AccommodationState.CANCELED);
    accommodation.setLastModified(LocalDate.now());

    log.info(String.format("ACCOMMODATION DAO - Canceled accommodation: %s.", accommodation));
    accommodationDao.update(accommodation);
    log.info("Successfully canceled!");
  }

}
