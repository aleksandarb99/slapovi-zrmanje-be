package com.slapovizrmanje.api.mapper;

import com.slapovizrmanje.api.model.CheckAvailabilityForCamp;
import com.slapovizrmanje.api.model.GuestsForCamp;
import com.slapovizrmanje.api.model.LodgingForCamp;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.util.Map;

@Component
public class CheckAvailabilityMapMapper {
  public CheckAvailabilityForCamp toEntity(Map<String, AttributeValue> item) {
    GuestsForCamp guest = GuestsForCamp.builder()
            .adults(Integer.parseInt(item.get("guests").m().get("adults").n()))
            .children(Integer.parseInt(item.get("guests").m().get("children").n()))
            .infants(Integer.parseInt(item.get("guests").m().get("infants").n()))
            .pets(Integer.parseInt(item.get("guests").m().get("pets").n()))
            .build();

    LodgingForCamp lodging = LodgingForCamp.builder()
            .car(Integer.parseInt(item.get("lodging").m().get("car").n()))
            .caravan(Integer.parseInt(item.get("lodging").m().get("caravan").n()))
            .tent(Integer.parseInt(item.get("lodging").m().get("tent").n()))
            .sleepingBag(Integer.parseInt(item.get("lodging").m().get("sleeping_bag").n()))
            .build();

    return CheckAvailabilityForCamp.builder()
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
