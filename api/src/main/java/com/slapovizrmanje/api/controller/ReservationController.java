package com.slapovizrmanje.api.controller;

import com.slapovizrmanje.shared.dto.CampRequestDTO;
import com.slapovizrmanje.api.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/check-availability/camp")
    public ResponseEntity<Void> checkAvailabilityForCamp(@Valid @RequestBody CampRequestDTO campRequestDTO) {
        log.info("RESERVATION SERVICE - Check availability for camp.");
        reservationService.checkAvailabilityForCamp(campRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
