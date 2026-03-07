package com.closer.backend.usuario.service;

import com.closer.backend.usuario.domain.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
  List<Usuario> findAll();

  Usuario findById(Long id);

  Optional<Usuario> findByUsername(String username);

  Usuario create(Usuario usuario);

  Usuario update(Long id, Usuario usuario);

  void delete(Long id);
}