package com.slapovizrmanje.api.util;

import com.slapovizrmanje.api.exception.BadRequestException;
import com.slapovizrmanje.shared.dto.GuestsDTO;
import com.slapovizrmanje.shared.model.enums.Language;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
public class Validator {

  public static void validateObjectToContainAtLeastOnePositive(Object object, Language language) {
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
        throw new BadRequestException(String.format(ExceptionMessages.getMessage(language,
                ExceptionMessageType.BadRequestExceptionFieldHasToBePositiveMessage), field.getName()));
      }
    }

    if (!isPositive) {
      throw new BadRequestException(String.format(ExceptionMessages.getMessage(language,
              ExceptionMessageType.BadRequestExceptionOneFieldMustBePositiveMessage), objectName));
    }
  }

  public static void validateMapToContainAtLeastOnePositive(Map<String, Integer> map, Language language) {
    log.info(String.format("Validating the %s.", map));
    boolean isPositive = false;
    for (String key : map.keySet()) {
      if (map.get(key) > 0) {
        isPositive = true;
      }
    }
    if (!isPositive) {
      throw new BadRequestException(String.format(ExceptionMessages.getMessage(language,
              ExceptionMessageType.BadRequestExceptionFieldHasToBePositiveMessage), map));
    }
  }

  public static void validateStartEndDate(LocalDate startDate, LocalDate endDate, Language language) {
    log.info(String.format("Validating the start date %s and end date %s ", startDate, endDate));
    if (!startDate.isBefore(endDate)) {
      throw new BadRequestException(ExceptionMessages.getMessage(language, ExceptionMessageType.BadRequestExceptionCheckOutMustBeAfterCheckInMessage));
    }
    if (startDate.isBefore(LocalDate.now())) {
      throw new BadRequestException(ExceptionMessages.getMessage(language, ExceptionMessageType.BadRequestExceptionCheckInMustBeInFutureMessage));

    }
  }


  public static void validateCapacity(Map<String, Integer> lodging, GuestsDTO guests, Language language) {
    int guestCount = countGuests(guests);
    int capacityCount = countLodgingCapacity(lodging);
    if (guestCount > capacityCount) {
      throw new BadRequestException(String.format(ExceptionMessages.getMessage(language, ExceptionMessageType.BadRequestExceptionOverCapacityMessage), guestCount, capacityCount));
    }
  }

  private static int countLodgingCapacity(Map<String, Integer> lodging) {
    int totalCount = 0;

    int apartment1 = lodging.getOrDefault("apartment1", 0);
    if (apartment1 != 0) {
      totalCount += apartment1 * AccommodationCapacity.apartment1Capacity;
    }

    int room1 = lodging.getOrDefault("room1", 0);
    if (room1 != 0) {
      totalCount += room1 * AccommodationCapacity.room1Capacity;
    }

    int room2 = lodging.getOrDefault("room2", 0);
    if (room2 != 0) {
      totalCount += room2 * AccommodationCapacity.room2Capacity;
    }

    int room3 = lodging.getOrDefault("room3", 0);
    if (room3 != 0) {
      totalCount += room3 * AccommodationCapacity.room3Capacity;
    }

    return totalCount;
  }

  private static int countGuests(GuestsDTO guestsDTO) {
    int totalCount = 0;
    totalCount += guestsDTO.getAdults();
    return totalCount;
  }
}
