package com.slapovizrmanje.shared.mapper;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.slapovizrmanje.shared.dto.AccommodationRequestDTO;
import com.slapovizrmanje.shared.dto.GuestsDTO;
import com.slapovizrmanje.shared.model.Accommodation;
import com.slapovizrmanje.shared.model.Guests;
import com.slapovizrmanje.shared.model.enums.AccommodationState;
import com.slapovizrmanje.shared.model.enums.AccommodationType;
import com.slapovizrmanje.shared.model.enums.Language;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.FIELD)
public interface AccommodationMapper {

  Accommodation toEntity(AccommodationRequestDTO accommodationRequestDTO);

  Guests toEntity(GuestsDTO guestsDTO);

  default Accommodation toEntity(Map<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue> item) {
    Guests guest = Guests.builder()
            .adults(Integer.parseInt(item.get("guests").m().get("adults").n()))
            .children(Integer.parseInt(item.get("guests").m().get("children").n()))
            .infants(Integer.parseInt(item.get("guests").m().get("infants").n()))
            .pets(Integer.parseInt(item.get("guests").m().get("pets").n()))
            .build();

    var lodgingMap = item.get("lodging").m();
    Map<String, Integer> finalLodgingMap = new HashMap<>();
    for (String key : lodgingMap.keySet()) {
      finalLodgingMap.put(key, Integer.parseInt(lodgingMap.get(key).n()));
    }

    return Accommodation.builder()
            .id(item.get("id").s())
            .email(item.get("email").s())
            .firstName(item.get("first_name").s())
            .lastName(item.get("last_name").s())
            .state(AccommodationState.valueOf(item.get("state").s()))
            .language(Language.valueOf(item.get("language").s()))
            .type(AccommodationType.valueOf(item.get("type").s()))
            .guests(guest)
            .lodging(finalLodgingMap)
            .code(item.get("code").s())
            .powerSupply(item.get("power_supply").bool())
            .createdAt(Long.parseLong(item.get("created_at").n()))
            .startDate(LocalDate.parse(item.get("start_date").s()))
            .endDate(LocalDate.parse(item.get("end_date").s()))
            .build();
  }

  default Accommodation eventToAccommodation(Map<String, AttributeValue> event) {
    Guests guests = Guests.builder()
            .adults(Integer.parseInt(event.get("guests").getM().get("adults").getN()))
            .children(Integer.parseInt(event.get("guests").getM().get("children").getN()))
            .infants(Integer.parseInt(event.get("guests").getM().get("infants").getN()))
            .pets(Integer.parseInt(event.get("guests").getM().get("pets").getN()))
            .build();

    Map<String, AttributeValue> lodgingMap = event.get("lodging").getM();
    Map<String, Integer> finalLodgingMap = new HashMap<>();
    for (String key : lodgingMap.keySet()) {
      finalLodgingMap.put(key, Integer.parseInt(lodgingMap.get(key).getN()));
    }

    return Accommodation.builder()
            .id(event.get("id").getS())
            .email(event.get("email").getS())
            .firstName(event.get("first_name").getS())
            .lastName(event.get("last_name").getS())
            .guests(guests)
            .lodging(finalLodgingMap)
            .code(event.get("code").getS())
            .powerSupply(event.get("power_supply").getBOOL())
            .createdAt(Long.parseLong(event.get("created_at").getN()))
            .startDate(LocalDate.parse(event.get("start_date").getS()))
            .endDate(LocalDate.parse(event.get("end_date").getS()))
            .state(AccommodationState.valueOf(event.get("state").getS()))
            .language(Language.valueOf(event.get("language").getS()))
            .type(AccommodationType.valueOf(event.get("type").getS()))
            .build();
  }
}
