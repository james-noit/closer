package com.closer.backend.usuario.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.closer.backend.usuario.domain.Usuario;
import com.closer.backend.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/api/usuarios")
public class UsuarioController {

  private final UsuarioService usuarioService;
  private final UsuarioModelAssembler usuarioModelAssembler;

  public UsuarioController(UsuarioService usuarioService, UsuarioModelAssembler usuarioModelAssembler) {
    this.usuarioService = usuarioService;
    this.usuarioModelAssembler = usuarioModelAssembler;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public CollectionModel<EntityModel<Usuario>> all() {
    List<EntityModel<Usuario>> usuarios = usuarioService.findAll()
        .stream()
        .map(usuarioModelAssembler::toModel)
        .toList();

    return CollectionModel.of(usuarios,
        linkTo(methodOn(UsuarioController.class).all()).withSelfRel());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('LOGGED_USER','ADMIN')")
  public EntityModel<Usuario> one(@PathVariable Long id) {
    Usuario usuario = usuarioService.findById(id);
    assertCanReadUsuario(usuario);
    return usuarioModelAssembler.toModel(usuario);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<EntityModel<Usuario>> create(@Valid @RequestBody Usuario usuario) {
    Usuario created = usuarioService.create(usuario);
    EntityModel<Usuario> model = usuarioModelAssembler.toModel(created);
    URI location = linkTo(methodOn(UsuarioController.class).one(created.getId())).toUri();
    return ResponseEntity.created(location).body(model);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<EntityModel<Usuario>> update(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
    Usuario updated = usuarioService.update(id, usuario);
    return ResponseEntity.ok(usuarioModelAssembler.toModel(updated));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    usuarioService.delete(id);
    return ResponseEntity.noContent().build();
  }

  private void assertCanReadUsuario(Usuario usuario) {
    if (isAdmin()) {
      return;
    }

    String current = currentUsername();
    if (current == null || !current.equals(usuario.getUsername())) {
      throw new AccessDeniedException("Solo puedes acceder a tu propio usuario.");
    }
  }

  private boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return false;
    }
    return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
  }

  private String currentUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return null;
    }
    return auth.getName();
  }
}