package com.slapovizrmanje.api.service;

import com.slapovizrmanje.api.dao.CheckAvailabilityForCampDao;
import com.slapovizrmanje.api.dto.CheckAvailabilityForCampDto;
import com.slapovizrmanje.api.mapper.CheckAvailabilityMapper;
import com.slapovizrmanje.api.model.CheckAvailabilityForCamp;
import com.slapovizrmanje.api.model.GuestsForCamp;
import com.slapovizrmanje.api.model.LodgingForCamp;
import com.slapovizrmanje.api.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
  private final CheckAvailabilityMapper checkAvailabilityMapper;
  private final CheckAvailabilityForCampDao checkAvailabilityForCampDao;

  public void checkAvailabilityForCamp(CheckAvailabilityForCampDto request) {
//    TODO: Validate request

    CheckAvailabilityForCamp entity = checkAvailabilityMapper.toEntity(request);
    entity.setId(UUID.randomUUID().toString());
    entity.setCreatedAt(System.currentTimeMillis());

    List<CheckAvailabilityForCamp> foundEntities = checkAvailabilityForCampDao.findByEmail(entity.getEmail());
    log.info(String.format("Found camp entities: %s", foundEntities));

    Optional<CheckAvailabilityForCamp> entityOptional = foundEntities
            .stream()
            .filter(foundEntity -> foundEntity.equals(entity))
            .findAny();
//    TODO: Check if data from request is already in foundEntities list
    if (entityOptional.isPresent()) {
      CheckAvailabilityForCamp foundEntity = entityOptional.get();
      log.info(String.format("Chosen camp entity: %s", foundEntity));
      LocalDateTime timeFromFoundEntity = TimeProvider.toLocalDateTime(foundEntity.getCreatedAt());
      log.info(String.format("Converted time: %s", timeFromFoundEntity));
      LocalDateTime currentTime = LocalDateTime.now();
      log.info(String.format("Current time: %s", currentTime));

//      TODO: Exceptions
      if (currentTime.minusMinutes(15).isBefore(timeFromFoundEntity)) {
        throw new RuntimeException("Cannot generate new one");
      }
    }

    log.info("Create camp reservation request: " + entity);
    checkAvailabilityForCampDao.create(entity);
    log.info("Successfully created: " + entity);
  }
}
