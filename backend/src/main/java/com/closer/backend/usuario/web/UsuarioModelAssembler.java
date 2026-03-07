package com.closer.backend.usuario.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.closer.backend.usuario.domain.Usuario;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UsuarioModelAssembler implements RepresentationModelAssembler<Usuario, EntityModel<Usuario>> {

  @Override
  public EntityModel<Usuario> toModel(Usuario usuario) {
    return EntityModel.of(usuario,
        linkTo(methodOn(UsuarioController.class).one(usuario.getId())).withSelfRel(),
        linkTo(methodOn(UsuarioController.class).all()).withRel("usuarios"));
  }
}