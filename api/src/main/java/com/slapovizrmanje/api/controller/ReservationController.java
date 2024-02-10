package com.slapovizrmanje.api.controller;

import com.slapovizrmanje.api.dto.CheckAvailabilityForCampDto;
import com.slapovizrmanje.api.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    @PostMapping("/check-availability/camp")
    public void checkAvailabilityForCamp(@RequestBody CheckAvailabilityForCampDto request) {
        reservationService.checkAvailabilityForCamp(request);
    }
}
