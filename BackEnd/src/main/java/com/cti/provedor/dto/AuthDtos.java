package com.cti.provedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record CadastroRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72) String senha,
        @Size(max = 30) String matricula,
        @Size(max = 60) String turma
    ) {
        @Override public String toString() { return "CadastroRequest[credenciais omitidas]"; }
    }

    // DTO com as credenciais enviadas pelo cliente.
    public record LoginRequest(@NotBlank String email, @NotBlank String senha) {
        @Override public String toString() { return "LoginRequest[credenciais omitidas]"; }
    }

    // A resposta devolve um token e dados públicos do usuário.
    public record LoginResponse(String token, String tipo, long expiraEmSegundos, UsuarioDto usuario) {}
}
