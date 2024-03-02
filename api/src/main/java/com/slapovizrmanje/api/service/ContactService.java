package com.slapovizrmanje.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slapovizrmanje.api.exception.BadRequestException;
import com.slapovizrmanje.shared.dto.ContactQuestionDTO;
import com.slapovizrmanje.shared.service.AwsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

  private final ObjectMapper objectMapper;
  private final AwsService awsService;

  public void getInTouch(ContactQuestionDTO contactQuestionDTO) {
    String message;
    try {
      log.info("OBJECT MAPPER - ContactQuestionDTO to String.");
      message = objectMapper.writeValueAsString(contactQuestionDTO);
    } catch (JsonProcessingException e) {
      throw new BadRequestException("Error occurred while converting ContactQuestionDTO to String.");
    }

    log.info("AWS SERVICE - Sending ContactQuestionDTO to email queue.");
    awsService.sendMessageToQueue(message, contactQuestionDTO.getClass().getSimpleName());
  }
}
