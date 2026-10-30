package com.cti.provedor.dto;

// A resposta devolve um token e dados públicos do usuário.
public record LoginResponse(String token, String tipo, UsuarioDto usuario) {}
