package com.cti.provedor.dto;

import jakarta.validation.constraints.NotBlank;

// DTO com as credenciais enviadas pelo cliente.
public record LoginRequest(@NotBlank String email, @NotBlank String senha) {
    @Override public String toString() { return "LoginRequest[credenciais omitidas]"; }
}
