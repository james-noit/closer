package com.closer.backend.grupoPersonas.service;

import com.closer.backend.grupoPersonas.domain.GrupoPersonas;
import com.closer.backend.grupoPersonas.repository.GrupoPersonasRepository;
import com.closer.backend.grupoPersonas.web.GrupoPersonasNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GrupoPersonasServiceImpl implements GrupoPersonasService {

    private final GrupoPersonasRepository grupoPersonasRepository;

    public GrupoPersonasServiceImpl(GrupoPersonasRepository grupoPersonasRepository) {
        this.grupoPersonasRepository = grupoPersonasRepository;
    }

    @Override
    public List<GrupoPersonas> findAll() {
        return grupoPersonasRepository.findAll();
    }

    @Override
    public GrupoPersonas findById(Long id) {
        return grupoPersonasRepository.findById(id)
                .orElseThrow(() -> new GrupoPersonasNotFoundException(id));
    }

    @Override
    public GrupoPersonas create(GrupoPersonas grupoPersonas) {
        return grupoPersonasRepository.save(grupoPersonas);
    }

    @Override
    public GrupoPersonas update(Long id, GrupoPersonas grupoPersonas) {
        GrupoPersonas existing = findById(id);
        existing.setNombre(grupoPersonas.getNombre());
        return grupoPersonasRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        GrupoPersonas existing = findById(id);
        grupoPersonasRepository.delete(existing);
    }
}
