package com.slapovizrmanje.api.controller;

import com.slapovizrmanje.api.exception.BadRequestException;
import com.slapovizrmanje.shared.exception.DbErrorException;
import com.slapovizrmanje.shared.dto.ExceptionMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ControllerAdvisor {
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BadRequestException.class)
  public ExceptionMessageDTO handleBadRequestException(final BadRequestException e) {
    log.info(String.format("BadRequestException -> %s.", e));
    return createExceptionMessage(e.getMessage());
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(DbErrorException.class)
  public ExceptionMessageDTO handleDbErrorException(final DbErrorException e) {
    log.error(String.format("BadRequestException -> %s.", e));
    return createExceptionMessage(e.getMessage());
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler({Exception.class})
  public ExceptionMessageDTO handleException(final Exception e) {
    log.error(String.format("Exception -> %s.", e));
    return createExceptionMessage(e.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({MethodArgumentNotValidException.class})
  protected ExceptionMessageDTO handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
    log.info(String.format("MethodArgumentNotValidException -> %s.", e));
    return createExceptionMessage(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({HttpMessageNotReadableException.class})
  protected ExceptionMessageDTO handleHttpMessageNotReadable(final HttpMessageNotReadableException e) {
    log.info(String.format("HttpMessageNotReadableException -> %s.", e));
    return createExceptionMessage(e.getMessage());
  }

  private ExceptionMessageDTO createExceptionMessage(final String message) {
    return ExceptionMessageDTO.builder()
            .timestamp(LocalDateTime.now())
            .message(message)
            .build();
  }
}
