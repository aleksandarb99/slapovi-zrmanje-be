package com.slapovizrmanje.api.controller;

import com.slapovizrmanje.api.service.AccommodationService;
import com.slapovizrmanje.api.service.ContactService;
import com.slapovizrmanje.api.service.PriceService;
import com.slapovizrmanje.shared.dto.*;
import com.slapovizrmanje.shared.model.enums.Language;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodation")
public class AccommodationController {

  private final AccommodationService accommodationService;
  private final PriceService priceService;
  private final ContactService contactService;

//  TODO: What to do with default error messages from framework

  @GetMapping("/cancel")
  public void cancel(@RequestParam String email, @RequestParam String id, @RequestParam String code, @RequestParam Language language) {
    log.info("ACCOMMODATION SERVICE - Cancel request.");
    accommodationService.cancel(email, id, code, language);
  }

  @GetMapping("/reserve")
  public void reserve(@RequestParam String email, @RequestParam String id, @RequestParam String code, @RequestParam Language language) {
    log.info("ACCOMMODATION SERVICE - Reserve request.");
    accommodationService.reserve(email, id, code, language);
  }

  @GetMapping("/accept")
  public void accept(@RequestParam String email, @RequestParam String id, @RequestParam String code, @RequestParam Language language) {
    log.info("ACCOMMODATION SERVICE - Accept request.");
    accommodationService.accept(email, id, code, language);
  }

  @GetMapping("/reject")
  public void reject(@RequestParam String email, @RequestParam String id, @RequestParam String code, @RequestParam Language language) {
    log.info("ACCOMMODATION SERVICE - Reject request.");
    accommodationService.reject(email, id, code, language);
  }

  @GetMapping("/verify")
  public void verifyEmail(@RequestParam String email, @RequestParam String id, @RequestParam String code, @RequestParam Language language) {
    log.info("ACCOMMODATION SERVICE - Verify email.");
    accommodationService.verifyEmail(email, id, code, language);
  }

  @PostMapping("/check-availability")
  @ResponseStatus(HttpStatus.CREATED)
  public void checkAvailability(@Valid @RequestBody AccommodationRequestDTO accommodationRequestDTO) {
    log.info("ACCOMMODATION SERVICE - Check availability.");
    accommodationService.checkAvailability(accommodationRequestDTO);
  }

  @PostMapping("/check-price/camp")
  public PriceResponseDTO checkPriceForCamp(@Valid @RequestBody CampPriceRequestDTO requestDTO) {
    log.info("PRICE SERVICE - Check price for camp.");
    return priceService.checkPriceForCamp(requestDTO);
  }

  @PostMapping("/check-price/room-or-apartment")
  public PriceResponseDTO checkPriceForRoomOrApartment(@Valid @RequestBody RoomOrApartmentPriceRequestDTO requestDTO) {
    log.info("PRICE SERVICE - Check price for room or apartment.");
    return priceService.checkPriceForRoomOrApartment(requestDTO);
  }

  @PostMapping("/get-in-touch")
  public void getInTouch(@Valid @RequestBody ContactQuestionDTO contactQuestionDTO) {
    log.info("CONTACT SERVICE - Get in touch.");
    contactService.getInTouch(contactQuestionDTO);
  }
}
