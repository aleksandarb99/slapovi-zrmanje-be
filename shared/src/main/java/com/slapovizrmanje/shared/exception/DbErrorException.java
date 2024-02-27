package com.slapovizrmanje.shared.exception;

public class DbErrorException extends RuntimeException {

  public DbErrorException(final String message) {
    super("Database operation failed: " + message);
  }
}
