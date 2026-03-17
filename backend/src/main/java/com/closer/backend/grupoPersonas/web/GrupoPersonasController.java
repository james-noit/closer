package com.closer.backend.grupoPersonas.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.closer.backend.grupoPersonas.domain.GrupoPersonas;
import com.closer.backend.grupoPersonas.service.GrupoPersonasService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grupos-personas")
public class GrupoPersonasController {

    private final GrupoPersonasService grupoPersonasService;
    private final GrupoPersonasModelAssembler grupoPersonasModelAssembler;

    public GrupoPersonasController(GrupoPersonasService grupoPersonasService,
            GrupoPersonasModelAssembler grupoPersonasModelAssembler) {
        this.grupoPersonasService = grupoPersonasService;
        this.grupoPersonasModelAssembler = grupoPersonasModelAssembler;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LOGGED_USER','ADMIN')")
    public CollectionModel<EntityModel<GrupoPersonas>> all() {
        List<EntityModel<GrupoPersonas>> grupos = grupoPersonasService.findAll()
                .stream()
                .map(grupoPersonasModelAssembler::toModel)
                .toList();

        return CollectionModel.of(grupos,
                linkTo(methodOn(GrupoPersonasController.class).all()).withSelfRel());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOGGED_USER','ADMIN')")
    public EntityModel<GrupoPersonas> one(@PathVariable Long id) {
        return grupoPersonasModelAssembler.toModel(grupoPersonasService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<GrupoPersonas>> create(@Valid @RequestBody GrupoPersonas grupoPersonas) {
        GrupoPersonas created = grupoPersonasService.create(grupoPersonas);
        EntityModel<GrupoPersonas> model = grupoPersonasModelAssembler.toModel(created);
        URI location = linkTo(methodOn(GrupoPersonasController.class).one(created.getId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<GrupoPersonas>> update(@PathVariable Long id,
            @Valid @RequestBody GrupoPersonas grupoPersonas) {
        GrupoPersonas updated = grupoPersonasService.update(id, grupoPersonas);
        return ResponseEntity.ok(grupoPersonasModelAssembler.toModel(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        grupoPersonasService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
