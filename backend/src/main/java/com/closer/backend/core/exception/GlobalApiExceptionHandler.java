package com.closer.backend.core.exception;

import com.closer.backend.grupoPersonas.web.GrupoPersonasNotFoundException;
import com.closer.backend.persona.web.PersonaNotFoundException;
import com.closer.backend.security.AccountLockedException;
import com.closer.backend.security.TooManyLoginRequestsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalApiExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

  @ExceptionHandler(PersonaNotFoundException.class)
  public ResponseEntity<ProblemDetail> handlePersonaNotFound(PersonaNotFoundException ex) {
    LOGGER.warn("Recurso no encontrado", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    detail.setTitle("Persona no encontrada");
    detail.setDetail(ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
  }

  @ExceptionHandler(GrupoPersonasNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleGrupoPersonasNotFound(GrupoPersonasNotFoundException ex) {
    LOGGER.warn("Recurso no encontrado", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    detail.setTitle("Grupo de personas no encontrado");
    detail.setDetail(ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
  }

  @ExceptionHandler(com.closer.backend.usuario.web.UsuarioNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleUsuarioNotFound(
      com.closer.backend.usuario.web.UsuarioNotFoundException ex) {
    LOGGER.warn("Recurso de usuario no encontrado", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    detail.setTitle("Usuario no encontrado");
    detail.setDetail(ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
    LOGGER.warn("Error de validacion en request", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Datos no validos");
    detail.setDetail("Revisa los campos enviados para Persona.");
    return ResponseEntity.badRequest().body(detail);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ProblemDetail> handleNotFound(NoResourceFoundException ex) {
    LOGGER.warn("Recurso HTTP no encontrado", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    detail.setTitle("Recurso no encontrado");
    detail.setDetail("La URL solicitada no existe.");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex) {
    LOGGER.warn("Credenciales inválidas", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    detail.setTitle("Credenciales inválidas");
    detail.setDetail("Usuario o contraseña incorrectos.");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(detail);
  }

  @ExceptionHandler(AuthenticationServiceException.class)
  public ResponseEntity<ProblemDetail> handleAuthenticationService(AuthenticationServiceException ex) {
    LOGGER.warn("Error de autenticación de servicio", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    detail.setTitle("Error de autenticación");
    detail.setDetail(ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(detail);
  }

  @ExceptionHandler(AccountLockedException.class)
  public ResponseEntity<ProblemDetail> handleAccountLocked(AccountLockedException ex) {
    LOGGER.warn("Cuenta bloqueada temporalmente", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.LOCKED);
    detail.setTitle("Cuenta bloqueada temporalmente");
    detail.setDetail(ex.getMessage());
    return ResponseEntity.status(HttpStatus.LOCKED).body(detail);
  }

  @ExceptionHandler(TooManyLoginRequestsException.class)
  public ResponseEntity<ProblemDetail> handleTooManyLoginRequests(TooManyLoginRequestsException ex) {
    LOGGER.warn("Rate limit de login excedido", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
    detail.setTitle("Demasiados intentos");
    detail.setDetail(ex.getMessage());
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(detail);
  }

  @ExceptionHandler(com.closer.backend.usuario.web.UsuarioAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleUsuarioExists(
      com.closer.backend.usuario.web.UsuarioAlreadyExistsException ex) {
    LOGGER.warn("Intento de crear usuario duplicado", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    detail.setTitle("Usuario ya existente");
    detail.setDetail(ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(detail);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
    LOGGER.warn("Acceso denegado", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    detail.setTitle("Acceso denegado");
    detail.setDetail("No tienes permisos para realizar esta acción.");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(detail);
  }

  @ExceptionHandler(HttpMessageNotWritableException.class)
  public ResponseEntity<ProblemDetail> handleSerializationError(HttpMessageNotWritableException ex) {
    LOGGER.error("Error de serialización de respuesta", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    detail.setTitle("Error interno del servidor");
    detail.setDetail("No se pudo serializar la respuesta HTTP.");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(detail);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
    LOGGER.error("Error no controlado", ex);

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    detail.setTitle("Error interno del servidor");
    detail.setDetail("Ha ocurrido un error inesperado.");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(detail);
  }
}
