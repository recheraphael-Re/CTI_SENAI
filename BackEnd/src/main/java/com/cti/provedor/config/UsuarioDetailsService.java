package com.cti.provedor.config;

import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository repository;

    public UsuarioDetailsService(UsuarioRepository repository) { this.repository = repository; }

    @Override
    public UserDetails loadUserByUsername(String email) {
        var usuario = repository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
        return User.withUsername(usuario.getEmail())
            .password(usuario.getSenhaHash())
            .authorities("ROLE_" + usuario.getPerfil().name())
            .disabled(usuario.getStatus() != StatusUsuario.ATIVO)
            .build();
    }
}
