package com.closer.backend.security;

import com.closer.backend.usuario.domain.Usuario;
import com.closer.backend.usuario.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenService.class);
  private static final int TOKEN_SIZE_BYTES = 48;
  private static final long REFRESH_TTL_DAYS = 7;

  private final RefreshTokenRepository refreshTokenRepository;
  private final UsuarioRepository usuarioRepository;

  public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
      UsuarioRepository usuarioRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.usuarioRepository = usuarioRepository;
  }

  @Transactional
  public String issueToken(Usuario usuario) {
    revokeAllActiveForUser(usuario.getId(), "new-login");

    String rawToken = generateSecureToken();
    RefreshToken token = new RefreshToken();
    token.setTokenHash(hash(rawToken));
    token.setUsuario(usuario);
    token.setCreatedAt(Instant.now());
    token.setExpiresAt(Instant.now().plus(REFRESH_TTL_DAYS, ChronoUnit.DAYS));
    refreshTokenRepository.save(token);

    LOGGER.info("Refresh token emitido para usuarioId={}", usuario.getId());
    return rawToken;
  }

  @Transactional
  public Usuario rotateToken(String rawToken) {
    RefreshToken current = findActive(rawToken)
        .orElseThrow(() -> new AuthenticationServiceException("Refresh token inválido o expirado"));

    String newRaw = generateSecureToken();
    String newHash = hash(newRaw);

    current.setRevokedAt(Instant.now());
    current.setReplacedByTokenHash(newHash);
    refreshTokenRepository.save(current);

    RefreshToken next = new RefreshToken();
    next.setTokenHash(newHash);
    next.setUsuario(current.getUsuario());
    next.setCreatedAt(Instant.now());
    next.setExpiresAt(Instant.now().plus(REFRESH_TTL_DAYS, ChronoUnit.DAYS));
    refreshTokenRepository.save(next);

    LOGGER.info("Refresh token rotado para usuarioId={}", current.getUsuario().getId());
    return current.getUsuario();
  }

  @Transactional
  public String rotateAndGetNewRawToken(String rawToken) {
    RefreshToken current = findActive(rawToken)
        .orElseThrow(() -> new AuthenticationServiceException("Refresh token inválido o expirado"));

    String newRaw = generateSecureToken();
    String newHash = hash(newRaw);

    current.setRevokedAt(Instant.now());
    current.setReplacedByTokenHash(newHash);
    refreshTokenRepository.save(current);

    RefreshToken next = new RefreshToken();
    next.setTokenHash(newHash);
    next.setUsuario(current.getUsuario());
    next.setCreatedAt(Instant.now());
    next.setExpiresAt(Instant.now().plus(REFRESH_TTL_DAYS, ChronoUnit.DAYS));
    refreshTokenRepository.save(next);

    LOGGER.info("Refresh token rotado para usuarioId={}", current.getUsuario().getId());
    return newRaw;
  }

  @Transactional
  public Optional<Long> revoke(String rawToken) {
    String hash = hash(rawToken);
    Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(hash);
    if (tokenOpt.isPresent()) {
      RefreshToken token = tokenOpt.get();
      if (token.getRevokedAt() == null) {
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
        LOGGER.info("Refresh token revocado para usuarioId={}", token.getUsuario().getId());
      }
      return Optional.of(token.getUsuario().getId());
    }
    LOGGER.warn("Intento de revocación con refresh token no encontrado");
    return Optional.empty();
  }

  @Transactional
  public void revokeAllActiveForUser(Long usuarioId, String reason) {
    List<RefreshToken> active = refreshTokenRepository.findByUsuarioIdAndRevokedAtIsNull(usuarioId);
    Instant now = Instant.now();
    for (RefreshToken token : active) {
      if (token.getExpiresAt().isAfter(now)) {
        token.setRevokedAt(now);
        refreshTokenRepository.save(token);
      }
    }
    if (!active.isEmpty()) {
      LOGGER.info("Revocados {} refresh tokens de usuarioId={} (reason={})", active.size(), usuarioId, reason);
    }
  }

  public Usuario getUserFromActiveToken(String rawToken) {
    RefreshToken token = findActive(rawToken)
        .orElseThrow(() -> new AuthenticationServiceException("Refresh token inválido o expirado"));
    return usuarioRepository.findById(token.getUsuario().getId())
        .orElseThrow(() -> new AuthenticationServiceException("Usuario no encontrado para refresh token"));
  }

  private Optional<RefreshToken> findActive(String rawToken) {
    String hash = hash(rawToken);
    Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(hash);
    if (tokenOpt.isEmpty()) {
      return Optional.empty();
    }
    RefreshToken token = tokenOpt.get();
    return token.isActive(Instant.now()) ? Optional.of(token) : Optional.empty();
  }

  private String generateSecureToken() {
    byte[] random = new byte[TOKEN_SIZE_BYTES];
    new java.security.SecureRandom().nextBytes(random);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
  }

  private String hash(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 no disponible", ex);
    }
  }
}
