package com.closer.backend.persona.web;

public class PersonaNotFoundException extends RuntimeException {

    public PersonaNotFoundException(Long id) {
        super("No se encontro Persona con id " + id);
    }
}
