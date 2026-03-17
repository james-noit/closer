package com.closer.backend.grupoPersonas.web;

public class GrupoPersonasNotFoundException extends RuntimeException {
    public GrupoPersonasNotFoundException(Long id) {
        super("Grupo de personas no encontrado con id: " + id);
    }
}
