package com.closer.backend.grupoPersonas.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.closer.backend.grupoPersonas.domain.GrupoPersonas;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class GrupoPersonasModelAssembler implements RepresentationModelAssembler<GrupoPersonas, EntityModel<GrupoPersonas>> {

    @Override
    public EntityModel<GrupoPersonas> toModel(GrupoPersonas grupoPersonas) {
        return EntityModel.of(grupoPersonas,
                linkTo(methodOn(GrupoPersonasController.class).one(grupoPersonas.getId())).withSelfRel(),
                linkTo(methodOn(GrupoPersonasController.class).all()).withRel("grupos-personas"));
    }
}
