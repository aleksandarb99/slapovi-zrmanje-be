package com.slapovizrmanje.api.util;

import com.slapovizrmanje.api.exception.BadRequestException;
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
    log.info("Validating dates.");
    if (!startDate.isBefore(endDate)) {
      throw new BadRequestException("Provided check out date has to be after the check in date.");
    }
    if (startDate.isBefore(LocalDate.now())) {
      throw new BadRequestException("Provided check in date has to be in future.");

    }
  }

}
