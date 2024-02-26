package com.slapovizrmanje.api.exception;

public class DbErrorException extends RuntimeException {

  public DbErrorException(final String message) {
    super("Database operation failed: " + message);
  }
}
