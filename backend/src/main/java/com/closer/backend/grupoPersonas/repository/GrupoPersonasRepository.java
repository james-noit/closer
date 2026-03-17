package com.closer.backend.grupoPersonas.repository;

import com.closer.backend.grupoPersonas.domain.GrupoPersonas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoPersonasRepository extends JpaRepository<GrupoPersonas, Long> {
}
