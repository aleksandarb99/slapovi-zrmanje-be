package com.slapovizrmanje.api.controller;

import com.slapovizrmanje.shared.dto.AccommodationRequestDTO;
import com.slapovizrmanje.api.service.AccommodationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodation")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping("/accept")
    public void acceptAccommodation(@RequestParam String email, @RequestParam String id, @RequestParam String code) {
        log.info("ACCOMMODATION SERVICE - Accept request.");
        accommodationService.acceptAccommodation(email, id, code);
    }
    @GetMapping("/reject")
    public void rejectAccommodation(@RequestParam String email, @RequestParam String id, @RequestParam String code) {
        log.info("ACCOMMODATION SERVICE - Reject request.");
        accommodationService.rejectAccommodation(email, id, code);
    }
    @GetMapping("/verify")
    public void verifyEmail(@RequestParam String email, @RequestParam String id, @RequestParam String code) {
        log.info("ACCOMMODATION SERVICE - Verify email.");
        accommodationService.verifyEmail(email, id, code);
    }

    @PostMapping("/check-availability")
    public ResponseEntity<Void> checkAvailabilityForCamp(@Valid @RequestBody AccommodationRequestDTO accommodationRequestDTO) {
        log.info("ACCOMMODATION SERVICE - Check availability.");
        accommodationService.checkAvailability(accommodationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
