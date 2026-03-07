package com.closer.backend.usuario.web;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(Long id) {
        super("No se encontró usuario con id " + id);
    }
}