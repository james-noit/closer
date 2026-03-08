package com.closer.backend.persona.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.closer.backend.persona.domain.Persona;
import com.closer.backend.persona.service.PersonaService;
import com.closer.backend.security.SecurityUserContext;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Objects;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('LOGGED_USER','ADMIN')")
    public CollectionModel<EntityModel<Persona>> all() {
        Long usuarioId = SecurityUserContext.currentUserIdOrNull();
        List<Persona> source = isAdmin() ? personaService.findAll() : personaService.findAllByUsuarioId(usuarioId);
        List<EntityModel<Persona>> personas = source
                .stream()
                .map(personaModelAssembler::toModel)
                .toList();

        return CollectionModel.of(personas,
                linkTo(methodOn(PersonaController.class).all()).withSelfRel());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOGGED_USER','ADMIN')")
    public EntityModel<Persona> one(@PathVariable Long id) {
        Persona persona = personaService.findById(id);
        assertCanAccessPersona(persona);
        return personaModelAssembler.toModel(persona);
    }

    private Long currentUserId() {
        return SecurityUserContext.currentUserIdOrNull();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LOGGED_USER','ADMIN')")
    public ResponseEntity<EntityModel<Persona>> create(@Valid @RequestBody Persona persona) {
        Long userId = currentUserId();
        if (isAdmin() && persona.getUsuario() != null && persona.getUsuario().getId() != null) {
            userId = persona.getUsuario().getId();
        }
        if (userId != null) {
            com.closer.backend.usuario.domain.Usuario u = new com.closer.backend.usuario.domain.Usuario();
            u.setId(userId);
            persona.setUsuario(u);
        }
        Persona created = personaService.create(persona);
        EntityModel<Persona> model = personaModelAssembler.toModel(created);
        URI location = linkTo(methodOn(PersonaController.class).one(created.getId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOGGED_USER','ADMIN')")
    public ResponseEntity<EntityModel<Persona>> update(@PathVariable Long id, @Valid @RequestBody Persona persona) {
        Persona existing = personaService.findById(id);
        assertCanAccessPersona(existing);
        // ignore any usuario field from request
        existing.setNombre(persona.getNombre());
        existing.setApellidos(persona.getApellidos());
        existing.setNumeroTelefono(persona.getNumeroTelefono());
        existing.setFechaCumpleanos(persona.getFechaCumpleanos());
        existing.setEmail(persona.getEmail());
        Persona updated = personaService.update(id, existing);
        return ResponseEntity.ok(personaModelAssembler.toModel(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOGGED_USER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Persona existing = personaService.findById(id);
        assertCanAccessPersona(existing);
        personaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private void assertCanAccessPersona(Persona persona) {
        if (isAdmin()) {
            return;
        }
        Long userId = currentUserId();
        if (persona.getUsuario() == null || !Objects.equals(userId, persona.getUsuario().getId())) {
            throw new AccessDeniedException("No tienes permisos para acceder a esta persona.");
        }
    }
}
