package com.slapovizrmanje.api.controller;

import com.slapovizrmanje.shared.dto.AccommodationRequestDTO;
import com.slapovizrmanje.api.service.AccommodationService;
import com.slapovizrmanje.shared.dto.PriceResponseDTO;
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
    @GetMapping("/cancel")
    public void cancel(@RequestParam String email, @RequestParam String id, @RequestParam String code) {
        log.info("ACCOMMODATION SERVICE - Cancel request.");
        accommodationService.cancel(email, id, code);
    }
    @GetMapping("/reserve")
    public void reserve(@RequestParam String email, @RequestParam String id, @RequestParam String code) {
        log.info("ACCOMMODATION SERVICE - Reserve request.");
        accommodationService.reserve(email, id, code);
    }

    @GetMapping("/accept")
    public void accept(@RequestParam String email, @RequestParam String id, @RequestParam String code) {
        log.info("ACCOMMODATION SERVICE - Accept request.");
        accommodationService.accept(email, id, code);
    }
    @GetMapping("/reject")
    public void reject(@RequestParam String email, @RequestParam String id, @RequestParam String code) {
        log.info("ACCOMMODATION SERVICE - Reject request.");
        accommodationService.reject(email, id, code);
    }
    @GetMapping("/verify")
    public void verifyEmail(@RequestParam String email, @RequestParam String id, @RequestParam String code) {
        log.info("ACCOMMODATION SERVICE - Verify email.");
        accommodationService.verifyEmail(email, id, code);
    }

    @PostMapping("/check-availability")
    public ResponseEntity<Void> checkAvailability(@Valid @RequestBody AccommodationRequestDTO accommodationRequestDTO) {
        log.info("ACCOMMODATION SERVICE - Check availability.");
        accommodationService.checkAvailability(accommodationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/check-price")
    public PriceResponseDTO checkPrice(@Valid @RequestBody AccommodationRequestDTO accommodationRequestDTO) {
        log.info("ACCOMMODATION SERVICE - Check availability.");
        return accommodationService.checkPrice(accommodationRequestDTO);
    }

}
