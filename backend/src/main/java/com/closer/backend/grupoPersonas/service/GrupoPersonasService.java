package com.closer.backend.grupoPersonas.service;

import com.closer.backend.grupoPersonas.domain.GrupoPersonas;
import java.util.List;

public interface GrupoPersonasService {
    List<GrupoPersonas> findAll();
    GrupoPersonas findById(Long id);
    GrupoPersonas create(GrupoPersonas grupoPersonas);
    GrupoPersonas update(Long id, GrupoPersonas grupoPersonas);
    void delete(Long id);
}
