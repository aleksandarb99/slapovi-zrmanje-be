package com.slapovizrmanje.api.service;

import com.slapovizrmanje.api.dao.CampRequestDao;
import com.slapovizrmanje.api.dto.CampRequestDTO;
import com.slapovizrmanje.api.exception.BadRequestException;
import com.slapovizrmanje.api.mapper.CampRequestMapper;
import com.slapovizrmanje.api.model.CampRequest;
import com.slapovizrmanje.api.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
  private final CampRequestMapper campRequestMapper;
  private final CampRequestDao campRequestDao;

  public void checkAvailabilityForCamp(CampRequestDTO campRequestDTO) {
//    TODO: Validate request

    log.info(String.format("CAMP REQUEST MAPPER - Convert to entity %s.", campRequestDTO));
    CampRequest campRequest = campRequestMapper.toEntity(campRequestDTO);
    campRequest.setId(UUID.randomUUID().toString());
    campRequest.setCreatedAt(System.currentTimeMillis());

    log.info("CAMP REQUEST DAO - Fetching by email.");
    List<CampRequest> foundEntities = campRequestDao.findByEmail(campRequest.getEmail());
    log.info(String.format("Found camp entities: %s", foundEntities));

    List<CampRequest> filteredCampRequests = foundEntities
            .stream()
            .filter(foundCampRequest -> foundCampRequest.equals(campRequest))
            .toList();

    if (!filteredCampRequests.isEmpty()) {
      LocalDateTime currentTime = LocalDateTime.now();
      filteredCampRequests
              .stream()
              .map(filteredCampRequest -> TimeProvider.toLocalDateTime(filteredCampRequest.getCreatedAt()))
              .filter(filteredTime -> currentTime.minusMinutes(15).isBefore(filteredTime))
              .findAny()
              .ifPresent(s -> {
                throw new BadRequestException("You have already raised a request.");
              });
    } else {
      log.info("There's no similar camp request recorded.");
    }

    log.info(String.format("CAMP REQUEST DAO - Create camp reservation request: %s.", campRequest));
    campRequestDao.create(campRequest);
    log.info("Successfully created!");
  }
}
