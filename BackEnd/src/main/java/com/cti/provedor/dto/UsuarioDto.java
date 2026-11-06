package com.cti.provedor.dto;

import com.cti.provedor.model.PerfilUsuario;
import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.model.Usuario;

// DTO de saída: nunca inclui senha nem hash.
public record UsuarioDto(Long id, String nome, String email, PerfilUsuario perfil, StatusUsuario status,
                         String matricula, String turma, java.time.Instant criadoEm) {
    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.getStatus(),
            u.getMatricula(), u.getTurma(), u.getCriadoEm());
    }
}
