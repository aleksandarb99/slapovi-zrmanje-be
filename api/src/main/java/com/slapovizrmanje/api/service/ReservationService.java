package com.slapovizrmanje.api.service;

import com.slapovizrmanje.api.dao.CheckAvailabilityForCampDao;
import com.slapovizrmanje.api.dto.CheckAvailabilityForCampDto;
import com.slapovizrmanje.api.mapper.CheckAvailabilityMapper;
import com.slapovizrmanje.api.model.CheckAvailabilityForCamp;
import com.slapovizrmanje.api.model.GuestsForCamp;
import com.slapovizrmanje.api.model.LodgingForCamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
  private final CheckAvailabilityMapper checkAvailabilityMapper;
  private final CheckAvailabilityForCampDao checkAvailabilityForCampDao;
  public void checkAvailabilityForCamp(CheckAvailabilityForCampDto request) {
//    TODO: Validate request


//    TODO: Mozda ovo ne treba :D
    GuestsForCamp entityGuests = checkAvailabilityMapper.toEntity(request.getGuests());
    LodgingForCamp entityLodging = checkAvailabilityMapper.toEntity(request.getLodging());
    CheckAvailabilityForCamp entity = checkAvailabilityMapper.toEntity(request);

    LocalDateTime createdAt = LocalDateTime.now();
    long timestamp = Timestamp.valueOf(createdAt).getTime();

    String id = UUID.randomUUID().toString();
    entity.setId(id);
    entity.setVerified(false);
    entity.setCreatedAt(timestamp);
    entity.setGuests(entityGuests);
    entity.setLodging(entityLodging);

    List<CheckAvailabilityForCamp> foundEntities = checkAvailabilityForCampDao.
            findByEmail(entity.getEmail());

//    TODO: Check if data from request is already in foundEntities list
    if (foundEntities.contains(entity)) {
//      TODO: Get that entity which is equal to this new one
      CheckAvailabilityForCamp foundEntity = null;
      Timestamp timestampFromFoundEntity = Timestamp.valueOf(String.valueOf(foundEntity.getCreatedAt()));
      LocalDateTime timeFromFoundEntity = timestampFromFoundEntity.toLocalDateTime();

      LocalDateTime currentTime = LocalDateTime.now();

//      TODO: Exceptions
      if (currentTime.minusMinutes(15).isAfter(timeFromFoundEntity)) {
        throw new RuntimeException("Cannot generate new one");
      }
    }

    log.info("Before entity creation : " + entity);
    checkAvailabilityForCampDao.create(entity);
    log.info("Successful creation : " + entity);
  }
}
