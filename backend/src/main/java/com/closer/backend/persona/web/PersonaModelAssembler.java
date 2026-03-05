package com.closer.backend.persona.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.closer.backend.persona.domain.Persona;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class PersonaModelAssembler implements RepresentationModelAssembler<Persona, EntityModel<Persona>> {

    @Override
    public EntityModel<Persona> toModel(Persona persona) {
        return EntityModel.of(persona,
                linkTo(methodOn(PersonaController.class).one(persona.getId())).withSelfRel(),
                linkTo(methodOn(PersonaController.class).all()).withRel("personas"));
    }
}
