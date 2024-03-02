package com.slapovizrmanje.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.shared.dao.AccommodationDao;
import com.slapovizrmanje.shared.dto.AccommodationsDTO;
import com.slapovizrmanje.shared.model.Accommodation;
import com.slapovizrmanje.shared.model.enums.ScheduledEmailType;
import com.slapovizrmanje.shared.service.AwsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Component
@Slf4j
public class ReminderSenderComponent {

  private AccommodationDao accommodationDao;
  private final ObjectMapper objectMapper;
  private final AwsService awsService;
  public Function<Object, Object> sendReminder() {
    return scheduledEvent -> {
      log.info("ACCOMMODATION DAO - Fetching entity where start date is tomorrow and state is reserved.");
      List<Accommodation> accommodationList = accommodationDao.findWhereStartDateIsTomorrowAndStateIsReserved();
      AccommodationsDTO accommodationsDTO = AccommodationsDTO.builder()
              .type(ScheduledEmailType.REMINDER)
              .accommodations(accommodationList)
              .build();
      String message;
      try {
        log.info("OBJECT MAPPER - Accommodation to String.");
        message = objectMapper.writeValueAsString(accommodationsDTO);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error occurred while converting Accommodation to String.");
      }

      awsService.sendMessageToQueue(message, accommodationsDTO.getClass().getSimpleName());
      log.info("Reminding owner of accommodation request which will start date is tomorrow.");
      return scheduledEvent;
    };
  }
}
