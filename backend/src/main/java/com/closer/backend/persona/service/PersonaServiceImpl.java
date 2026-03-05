package com.closer.backend.persona.service;

import com.closer.backend.persona.domain.Persona;
import com.closer.backend.persona.repository.PersonaRepository;
import com.closer.backend.persona.web.PersonaNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;

    public PersonaServiceImpl(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    public List<Persona> findAll() {
        return personaRepository.findAll();
    }

    @Override
    public Persona findById(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));
    }

    @Override
    public Persona create(Persona persona) {
        return personaRepository.save(persona);
    }

    @Override
    public Persona update(Long id, Persona persona) {
        Persona existing = findById(id);
        // Mantiene la identidad y solo actualiza los campos editables.
        existing.setNombre(persona.getNombre());
        existing.setApellidos(persona.getApellidos());
        existing.setNumeroTelefono(persona.getNumeroTelefono());
        existing.setFechaCumpleanos(persona.getFechaCumpleanos());
        existing.setEmail(persona.getEmail());
        return personaRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Persona existing = findById(id);
        personaRepository.delete(existing);
    }
}
