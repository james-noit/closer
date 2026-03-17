package com.closer.backend.persona.repository;

import com.closer.backend.persona.domain.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    java.util.List<Persona> findByUsuarioId(Long usuarioId);
    java.util.List<Persona> findByGrupoPersonasId(Long grupoPersonasId);
}
