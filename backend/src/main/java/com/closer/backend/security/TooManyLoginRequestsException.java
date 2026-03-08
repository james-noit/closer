package com.closer.backend.security;

public class TooManyLoginRequestsException extends RuntimeException {
  public TooManyLoginRequestsException(String message) {
    super(message);
  }
}
