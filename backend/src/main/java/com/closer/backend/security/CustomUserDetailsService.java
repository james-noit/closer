package com.closer.backend.security;

import com.closer.backend.usuario.domain.Usuario;
import com.closer.backend.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Security and login both rely on this lookup; keep the error explicit.
        Optional<Usuario> optional = usuarioRepository.findByUsername(username);
        if (optional.isEmpty()) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }
        return new CustomUserDetails(optional.get());
    }
}