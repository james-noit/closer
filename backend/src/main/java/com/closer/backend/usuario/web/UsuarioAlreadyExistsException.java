package com.closer.backend.usuario.web;

public class UsuarioAlreadyExistsException extends RuntimeException {
  public UsuarioAlreadyExistsException(String username) {
    super("Ya existe un usuario con username='" + username + "'");
  }
}
