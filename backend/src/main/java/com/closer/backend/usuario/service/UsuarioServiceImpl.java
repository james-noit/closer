package com.closer.backend.usuario.service;

import com.closer.backend.persona.domain.Persona;
import com.closer.backend.usuario.domain.Usuario;
import com.closer.backend.usuario.domain.UserRole;
import com.closer.backend.usuario.repository.UsuarioRepository;
import com.closer.backend.usuario.web.UsuarioNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

  private final UsuarioRepository usuarioRepository;

  public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  public List<Usuario> findAll() {
    return usuarioRepository.findAll();
  }

  @Override
  public Usuario findById(Long id) {
    return usuarioRepository.findById(id)
        .orElseThrow(() -> new UsuarioNotFoundException(id));
  }

  @Override
  public Optional<Usuario> findByUsername(String username) {
    return usuarioRepository.findByUsername(username);
  }

  @Override
  public Usuario create(Usuario usuario) {
    // if the username already exists, fail early with a custom exception
    usuarioRepository.findByUsername(usuario.getUsername()).ifPresent(u -> {
      throw new com.closer.backend.usuario.web.UsuarioAlreadyExistsException(usuario.getUsername());
    });

    if (usuario.getRole() == null) {
      usuario.setRole(UserRole.ROLE_LOGGED_USER);
    }
    return usuarioRepository.save(usuario);
  }

  @Override
  public Usuario update(Long id, Usuario usuario) {
    Usuario existing = findById(id);
    existing.setUsername(usuario.getUsername());
    existing.setPassword(usuario.getPassword());
    existing.setPersona(usuario.getPersona());
    existing.setRole(usuario.getRole() == null ? existing.getRole() : usuario.getRole());
    return usuarioRepository.save(existing);
  }

  @Override
  public void delete(Long id) {
    Usuario existing = findById(id);
    usuarioRepository.delete(existing);
  }
}