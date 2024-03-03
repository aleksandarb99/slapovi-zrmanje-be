package com.slapovizrmanje.api.service;

import com.slapovizrmanje.api.exception.BadRequestException;
import com.slapovizrmanje.api.util.AccommodationCapacity;
import com.slapovizrmanje.api.util.Prices;
import com.slapovizrmanje.api.util.Validator;
import com.slapovizrmanje.shared.dto.*;
import com.slapovizrmanje.shared.model.enums.AccommodationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceService {

  public PriceResponseDTO checkPriceForCamp(CampPriceRequestDTO requestDTO) {
    Validator.validateObjectToContainAtLeastOnePositive(requestDTO.getGuests());
    Validator.validateMapToContainAtLeastOnePositive(requestDTO.getLodging());
    Validator.validateStartEndDate(requestDTO.getStartDate(), requestDTO.getEndDate());

    PriceResponseDTO responseDTO = PriceResponseDTO.builder()
            .priceItems(new ArrayList<>())
            .build();

    long numberOfNights = DAYS.between(requestDTO.getStartDate(), requestDTO.getEndDate());

    calculatePriceForCamp(requestDTO, responseDTO, numberOfNights);

    return responseDTO;
  }

  public PriceResponseDTO checkPriceForRoomOrApartment(RoomOrApartmentPriceRequestDTO requestDTO) {
    Validator.validateObjectToContainAtLeastOnePositive(requestDTO.getGuests());
    Validator.validateMapToContainAtLeastOnePositive(requestDTO.getLodging());
    Validator.validateStartEndDate(requestDTO.getStartDate(), requestDTO.getEndDate());
    //    TODO: Test this
    Validator.validateCapacity(requestDTO.getGuests(), requestDTO.getLodging());

    if (!requestDTO.getType().equals(AccommodationType.APARTMENT) && !requestDTO.getType().equals(AccommodationType.ROOM)) {
      throw new BadRequestException("Request type is not valid");
    }

    PriceResponseDTO responseDTO = PriceResponseDTO.builder()
            .priceItems(new ArrayList<>())
            .build();

    long numberOfNights = DAYS.between(requestDTO.getStartDate(), requestDTO.getEndDate());

    if (requestDTO.getType().equals(AccommodationType.APARTMENT)) {
      calculatePriceForApartment(requestDTO, responseDTO, numberOfNights);
    }
    if (requestDTO.getType().equals(AccommodationType.ROOM)) {
      calculatePriceForRoom(requestDTO, responseDTO, numberOfNights);
    }

    return responseDTO;
  }

  private void calculatePriceForCamp(CampPriceRequestDTO request, PriceResponseDTO responseDTO, long numberOfNights) {
    double totalPrice = 0;
    int adults = request.getGuests().getAdults();
    if (adults != 0) {
      double price = Prices.adultsPrice * numberOfNights * adults;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Adults")
              .count(adults)
              .nights(numberOfNights)
              .price(price)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }
    int children = request.getGuests().getChildren();
    if (children != 0) {
      double price = Prices.childrenPrice * numberOfNights * children;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Children")
              .price(price)
              .count(children)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }
    int infants = request.getGuests().getInfants();
    if (infants != 0) {
      double price = Prices.infantsPrice * numberOfNights * infants;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Infants")
              .price(price)
              .count(infants)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }
    int pets = request.getGuests().getPets();
    if (pets != 0) {
      double price = Prices.petsPrice * numberOfNights * pets;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Pets")
              .price(price)
              .count(pets)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }
    int caravan = request.getLodging().get("caravan");
    if (caravan != 0) {
      double price = Prices.caravanPrice * numberOfNights * caravan;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Caravan")
              .price(price)
              .count(caravan)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }
    int tent = request.getLodging().get("tent");
    if (tent != 0) {
      double price = Prices.tentPrice * numberOfNights * tent;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Tent")
              .price(price)
              .count(tent)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }
    int car = request.getLodging().get("car");
    if (car != 0) {
      double price = Prices.carPrice * numberOfNights * car;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Car")
              .price(price)
              .count(car)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }
    int sleepingBag = request.getLodging().get("sleepingBag");
    if (sleepingBag != 0) {
      double price = Prices.sleepingBagPrice * numberOfNights * sleepingBag;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("SleepingBag")
              .price(price)
              .count(sleepingBag)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }
    if (request.isPowerSupply()) {
      double price = Prices.powerSupplyCost;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("PowerSupply")
              .price(price)
              .count(1)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }

    responseDTO.setTotalPrice(totalPrice);
  }

  private void calculatePriceForApartment(RoomOrApartmentPriceRequestDTO requestDTO, PriceResponseDTO responseDTO, long numberOfNights) {
    int apartment1 = requestDTO.getLodging().get("apartment1");
    double totalPrice = 0;
    if (apartment1 != 0) {
      double price = Prices.apartment1Price * numberOfNights;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Apartment 1")
              .price(price)
              .count(1)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }

    responseDTO.setTotalPrice(totalPrice);
  }

  private void calculatePriceForRoom(RoomOrApartmentPriceRequestDTO requestDTO, PriceResponseDTO responseDTO, long numberOfNights) {
    double totalPrice = 0;
    int room1 = requestDTO.getLodging().get("room1");
    if (room1 != 0) {
      double price = Prices.room1Price * numberOfNights;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Room 1")
              .price(price)
              .count(1)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }
    int room2 = requestDTO.getLodging().get("room2");
    if (room2 != 0) {
      double price = Prices.room2Price * numberOfNights;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Room 2")
              .price(price)
              .count(1)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }
    int room3 = requestDTO.getLodging().get("room3");
    if (room3 != 0) {
      double price = Prices.room3Price * numberOfNights;
      PriceItemDTO itemDTO = PriceItemDTO.builder()
              .name("Room 3")
              .price(price)
              .count(1)
              .nights(numberOfNights)
              .build();
      totalPrice += price;
      responseDTO.getPriceItems().add(itemDTO);
    }

    responseDTO.setTotalPrice(totalPrice);
  }

}
