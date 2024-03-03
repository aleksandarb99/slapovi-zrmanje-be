package com.slapovizrmanje.api.util;

import com.slapovizrmanje.api.exception.BadRequestException;
import com.slapovizrmanje.shared.dto.GuestsDTO;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
public class Validator {

  public static void validateObjectToContainAtLeastOnePositive(Object object) {
    String objectName = object.getClass().getSimpleName();
    log.info(String.format("Validating the %s.", objectName));
    boolean isPositive = false;
    Field[] declaredFields = object.getClass().getDeclaredFields();
    for (Field field : declaredFields) {
      field.setAccessible(true);
      try {
        int chosenFieldValue = field.getInt(object);
        if (chosenFieldValue > 0) {
          isPositive = true;
          break;
        }
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new BadRequestException(String.format("Field - %s has to be a positive value.", field.getName()));
      }
    }

    if (!isPositive) {
      throw new BadRequestException(String.format("%s - At least one field has to be a positive value!", objectName));
    }
  }

  public static void validateMapToContainAtLeastOnePositive(Map<String, Integer> map) {
    log.info(String.format("Validating the %s.", map));
    boolean isPositive = false;
    for (String key : map.keySet()) {
      if (map.get(key) > 0) {
        isPositive = true;
      }
    }
    if (!isPositive) {
      throw new BadRequestException(String.format("%s - At least one field has to be a positive value!", map));
    }
  }

  public static void validateStartEndDate(LocalDate startDate, LocalDate endDate) {
    log.info(String.format("Validating the start date %s and end date %s ", startDate, endDate));
    if (!startDate.isBefore(endDate)) {
      throw new BadRequestException("Provided check out date has to be after the check in date.");
    }
    if (startDate.isBefore(LocalDate.now())) {
      throw new BadRequestException("Provided check in date has to be in future.");

    }
  }


  public static void validateCapacity(GuestsDTO guests, Map<String, Integer> lodging) {
    int guestCount = countGuests(guests);
    int capacityCount = countLodgingCapacity(lodging);
    if (guestCount > capacityCount) {
      throw new BadRequestException(String.format("Request contains %s people but selected" +
              " lodging max capacity is %s.", guestCount, capacityCount));
    }
  }

  private static int countLodgingCapacity(Map<String, Integer> lodging) {
    int totalCount = 0;

    int apartment1 = lodging.get("apartment1");
    if (apartment1 != 0) {
      totalCount += apartment1 * AccommodationCapacity.apartment1Capacity;
    }

    int room1 = lodging.get("room1");
    if (apartment1 != 0) {
      totalCount += room1 * AccommodationCapacity.room1Capacity;
    }

    int room2 = lodging.get("room2");
    if (room2 != 0) {
      totalCount += room2 * AccommodationCapacity.room2Capacity;
    }

    int room3 = lodging.get("room3");
    if (room3 != 0) {
      totalCount += room3 * AccommodationCapacity.room3Capacity;
    }
//    TODO: Finish this

    return totalCount;
  }

  private static int countGuests(GuestsDTO guests) {
    int totalCount = 0;

    totalCount += guests.getAdults();
//    TODO: Finish this

    return totalCount;
  }
}
