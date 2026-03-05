package com.closer.backend.persona.repository;

import com.closer.backend.persona.domain.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
}
