package com.slapovizrmanje.api.util;

// I dont use upper case intentionally
public enum ExceptionMessageType {
  NotFoundExceptionMessage,
  BadRequestExceptionInvalidStateMessage,
  BadRequestExceptionInvalidCodeMessage,
  BadRequestExceptionCannotCancelMessage,
  BadRequestExceptionAlreadySentRequestMessage,
  BadRequestExceptionStartDateNotInTheFutureMessage,
  BadRequestExceptionBadTypeMessage,
  BadRequestExceptionFieldHasToBePositiveMessage,
  BadRequestExceptionOneFieldMustBePositiveMessage,
  BadRequestExceptionCheckOutMustBeAfterCheckInMessage,
  BadRequestExceptionCheckInMustBeInFutureMessage,
  BadRequestExceptionOverCapacityMessage,
}
