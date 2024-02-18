package com.slapovizrmanje.shared.mapper;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.slapovizrmanje.shared.dto.CampGuestsDTO;
import com.slapovizrmanje.shared.dto.CampLodgingDTO;
import com.slapovizrmanje.shared.dto.CampRequestDTO;
import com.slapovizrmanje.shared.model.CampGuests;
import com.slapovizrmanje.shared.model.CampLodging;
import com.slapovizrmanje.shared.model.CampRequest;
import com.slapovizrmanje.shared.model.Notification;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.FIELD)
public interface CampRequestMapper {

  CampRequest toEntity(CampRequestDTO campRequestDTO);

  CampGuests toEntity(CampGuestsDTO campGuestsDTO);

  CampLodging toEntity(CampLodgingDTO campLodgingDTO);

  default CampRequest toEntity(Map<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue> item) {
    CampGuests guest = CampGuests.builder()
            .adults(Integer.parseInt(item.get("guests").m().get("adults").n()))
            .children(Integer.parseInt(item.get("guests").m().get("children").n()))
            .infants(Integer.parseInt(item.get("guests").m().get("infants").n()))
            .pets(Integer.parseInt(item.get("guests").m().get("pets").n()))
            .build();

    CampLodging lodging = CampLodging.builder()
            .car(Integer.parseInt(item.get("lodging").m().get("car").n()))
            .caravan(Integer.parseInt(item.get("lodging").m().get("caravan").n()))
            .tent(Integer.parseInt(item.get("lodging").m().get("tent").n()))
            .sleepingBag(Integer.parseInt(item.get("lodging").m().get("sleeping_bag").n()))
            .build();

    return CampRequest.builder()
            .id(item.get("id").s())
            .email(item.get("email").s())
            .firstName(item.get("first_name").s())
            .lastName(item.get("last_name").s())
            .isVerified(item.get("verified").bool())
            .guests(guest)
            .lodging(lodging)
            .powerSupply(item.get("power_supply").bool())
            .createdAt(Long.parseLong(item.get("created_at").n()))
            .startDate(LocalDate.parse(item.get("start_date").s()))
            .endDate(LocalDate.parse(item.get("end_date").s()))
            .build();
  }

  default Notification eventToNotification(Map<String, AttributeValue> item) {
    Map<String, AttributeValue> guestsMap = item.get("guests").getM();
    Map<String, Integer> finalGuestsMap = new HashMap<>();
    List<String> guestTypes = List.of("adults", "children", "infants", "pets");
    guestTypes.forEach(type -> {
      int guestNumber = Integer.parseInt(guestsMap.get(type).getN());
      if (guestNumber > 0) {
        finalGuestsMap.put(type, guestNumber);
      }
    });

    Map<String, AttributeValue> lodgingMap = item.get("lodging").getM();
    Map<String, Integer> finalLodgingMap = new HashMap<>();
    List<String> lodgingTypes = List.of("car", "caravan", "tent", "sleeping_bag");
    lodgingTypes.forEach(type -> {
      int lodgingNumber = Integer.parseInt(lodgingMap.get(type).getN());
      if (lodgingNumber > 0) {
        finalLodgingMap.put(type, lodgingNumber);
      }
    });

    return Notification.builder()
            .recordId(item.get("id").getS().split("#")[1])  // type#UUID
            .email(item.get("email").getS())
            .firstName(item.get("first_name").getS())
            .lastName(item.get("last_name").getS())
            .guests(finalGuestsMap)
            .lodging(finalLodgingMap)
            .powerSupply(item.get("power_supply").getBOOL())
            .createdAt(Long.parseLong(item.get("created_at").getN()))
            .startDate(item.get("start_date").getS())
            .endDate(item.get("end_date").getS())
            .verified(item.get("verified").getBOOL())
            .build();
  }
}
