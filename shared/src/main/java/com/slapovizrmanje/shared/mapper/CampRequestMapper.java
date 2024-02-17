package com.slapovizrmanje.shared.mapper;

import com.slapovizrmanje.shared.dto.CampGuestsDTO;
import com.slapovizrmanje.shared.dto.CampLodgingDTO;
import com.slapovizrmanje.shared.dto.CampRequestDTO;
import com.slapovizrmanje.shared.model.CampGuests;
import com.slapovizrmanje.shared.model.CampLodging;
import com.slapovizrmanje.shared.model.CampRequest;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.util.Map;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.FIELD)
public interface CampRequestMapper {

  CampRequest toEntity(CampRequestDTO campRequestDTO);

  CampGuests toEntity(CampGuestsDTO campGuestsDTO);

  CampLodging toEntity(CampLodgingDTO campLodgingDTO);

  default CampRequest toEntity(Map<String, AttributeValue> item) {
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
}
