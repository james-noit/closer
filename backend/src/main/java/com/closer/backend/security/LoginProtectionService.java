package com.closer.backend.security;

import com.closer.backend.usuario.domain.Usuario;
import com.closer.backend.usuario.repository.UsuarioRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginProtectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoginProtectionService.class);

  private static final int MAX_LOGIN_REQUESTS_PER_MINUTE_BY_IP = 20;
  private static final int MAX_FAILED_ATTEMPTS_BEFORE_LOCK = 5;
  private static final long LOCK_MINUTES = 15;

  private final UsuarioRepository usuarioRepository;
  private final ConcurrentHashMap<String, Deque<Long>> requestWindowByIp = new ConcurrentHashMap<>();

  public LoginProtectionService(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @Transactional
  public void validateLoginAttempt(String username, String clientIp) {
    guardRateLimitByIp(clientIp);

    Optional<Usuario> userOpt = usuarioRepository.findByUsername(username);
    if (userOpt.isPresent()) {
      Usuario user = userOpt.get();
      if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(Instant.now())) {
        LOGGER.warn("Intento de login bloqueado para username={} ip={}", username, clientIp);
        throw new AccountLockedException("Cuenta temporalmente bloqueada. Inténtalo más tarde.");
      }
    }
  }

  @Transactional
  public void onFailedLogin(String username, String clientIp) {
    Optional<Usuario> userOpt = usuarioRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      LOGGER.warn("Login fallido para username inexistente={} ip={}", username, clientIp);
      return;
    }

    Usuario user = userOpt.get();
    int attempts = user.getFailedLoginAttempts() + 1;
    user.setFailedLoginAttempts(attempts);
    if (attempts >= MAX_FAILED_ATTEMPTS_BEFORE_LOCK) {
      user.setLockoutUntil(Instant.now().plus(LOCK_MINUTES, ChronoUnit.MINUTES));
      user.setFailedLoginAttempts(0);
      LOGGER.warn("Cuenta bloqueada temporalmente username={} ip={} minutos={}", username, clientIp, LOCK_MINUTES);
    } else {
      LOGGER.warn("Login fallido username={} ip={} intentosFallidos={}", username, clientIp, attempts);
    }
    usuarioRepository.save(user);
  }

  @Transactional
  public void onSuccessfulLogin(String username, String clientIp) {
    Optional<Usuario> userOpt = usuarioRepository.findByUsername(username);
    if (userOpt.isPresent()) {
      Usuario user = userOpt.get();
      user.setFailedLoginAttempts(0);
      user.setLockoutUntil(null);
      usuarioRepository.save(user);
    }
    LOGGER.info("Login exitoso username={} ip={}", username, clientIp);
  }

  // Tests run in the same Spring context, so this reset keeps scenarios isolated.
  void clearInMemoryStateForTests() {
    requestWindowByIp.clear();
  }

  private void guardRateLimitByIp(String clientIp) {
    long now = System.currentTimeMillis();
    long windowStart = now - 60_000;

    Deque<Long> queue = requestWindowByIp.computeIfAbsent(clientIp, ignored -> new LinkedList<>());
    synchronized (queue) {
      while (!queue.isEmpty() && queue.peekFirst() < windowStart) {
        queue.pollFirst();
      }
      if (queue.size() >= MAX_LOGIN_REQUESTS_PER_MINUTE_BY_IP) {
        LOGGER.warn("Rate limit de login excedido ip={} requestsUltimoMinuto={}", clientIp, queue.size());
        throw new TooManyLoginRequestsException("Demasiados intentos de login. Inténtalo más tarde.");
      }
      queue.addLast(now);
    }
  }
}
