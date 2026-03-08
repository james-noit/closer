package com.closer.backend.security;

public class AccountLockedException extends RuntimeException {
  public AccountLockedException(String message) {
    super(message);
  }
}
