package com.closer.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tiny helper to keep current-user extraction in one place.
 */
public final class SecurityUserContext {

  private SecurityUserContext() {
  }

  public static Long currentUserIdOrNull() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
      return userDetails.getId();
    }
    return null;
  }
}
