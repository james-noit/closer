package com.closer.backend.persona.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.closer.backend.persona.domain.Persona;
import com.closer.backend.persona.service.PersonaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personas")
public class PersonaController {

    private final PersonaService personaService;
    private final PersonaModelAssembler personaModelAssembler;

    public PersonaController(PersonaService personaService, PersonaModelAssembler personaModelAssembler) {
        this.personaService = personaService;
        this.personaModelAssembler = personaModelAssembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<Persona>> all() {
        // Cada recurso se devuelve con enlaces HATEOAS para auto-descubrimiento.
        List<EntityModel<Persona>> personas = personaService.findAll()
                .stream()
                .map(personaModelAssembler::toModel)
                .toList();

        return CollectionModel.of(personas,
                linkTo(methodOn(PersonaController.class).all()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<Persona> one(@PathVariable Long id) {
        Persona persona = personaService.findById(id);
        return personaModelAssembler.toModel(persona);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Persona>> create(@Valid @RequestBody Persona persona) {
        Persona created = personaService.create(persona);
        EntityModel<Persona> model = personaModelAssembler.toModel(created);
        // Location apunta al recurso creado, siguiendo semantica REST.
        URI location = linkTo(methodOn(PersonaController.class).one(created.getId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Persona>> update(@PathVariable Long id, @Valid @RequestBody Persona persona) {
        Persona updated = personaService.update(id, persona);
        return ResponseEntity.ok(personaModelAssembler.toModel(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
