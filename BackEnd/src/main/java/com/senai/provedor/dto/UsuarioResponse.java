package com.senai.provedor.dto;

import com.senai.provedor.models.Perfil;
import com.senai.provedor.models.StatusUsuario;
import com.senai.provedor.models.Usuario;

// DTO de saída: nunca inclui senha nem hash.
public record UsuarioResponse(Long id, String nome, String email, Perfil perfil, StatusUsuario status) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.getStatus());
    }
}
