package com.slapovizrmanje.api.service;

import com.slapovizrmanje.api.dto.CheckAvailabilityForCampDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReservationService {
  public void checkAvailabilityForCamp(CheckAvailabilityForCampDto request) {
//    TODO: Validate request
//    TODO: Save request to db
//    TODO: Generate UUID and send email to SQS
    log.info(String.valueOf(request));
    log.info("It works!");
  }
}
