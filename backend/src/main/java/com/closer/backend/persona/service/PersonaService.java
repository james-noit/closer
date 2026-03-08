package com.closer.backend.persona.service;

import com.closer.backend.persona.domain.Persona;
import java.util.List;

public interface PersonaService {

    List<Persona> findAll();

    List<Persona> findAllByUsuarioId(Long usuarioId);

    Persona findById(Long id);

    Persona create(Persona persona);

    Persona update(Long id, Persona persona);

    void delete(Long id);
}
