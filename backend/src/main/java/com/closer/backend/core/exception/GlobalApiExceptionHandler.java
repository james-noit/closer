package com.closer.backend.core.exception;

import com.closer.backend.persona.web.PersonaNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalApiExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

  @ExceptionHandler(PersonaNotFoundException.class)
  public ResponseEntity<ProblemDetail> handlePersonaNotFound(PersonaNotFoundException ex) {
    LOGGER.warn("Recurso no encontrado: {}", ex.getMessage());

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    detail.setTitle("Persona no encontrada");
    detail.setDetail(ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
    LOGGER.warn("Error de validacion en request: {}", ex.getMessage());

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Datos no validos");
    detail.setDetail("Revisa los campos enviados para Persona.");
    return ResponseEntity.badRequest().body(detail);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ProblemDetail> handleNotFound(NoResourceFoundException ex) {
    LOGGER.warn("Recurso HTTP no encontrado: {}", ex.getMessage());

    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    detail.setTitle("Recurso no encontrado");
    detail.setDetail("La URL solicitada no existe.");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
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
