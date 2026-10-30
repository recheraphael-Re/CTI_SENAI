package com.cti.provedor.service;

import com.cti.provedor.dto.*;
import com.cti.provedor.model.PerfilUsuario;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.UsuarioRepository;
import com.cti.provedor.config.JwtService;
import java.nio.charset.StandardCharsets;
import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.config.ApiException;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service // Regras de negócio do cadastro e do login.
public class AuthService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository repository, PasswordEncoder encoder, JwtService jwt) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @org.springframework.transaction.annotation.Transactional
    public UsuarioDto cadastrar(CadastroRequest dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        // BCrypt considera no máximo 72 bytes; comprimento não equivale a bytes em UTF-8.
        if (dados.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha muito longa para BCrypt");
        }
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_JA_CADASTRADO", "E-mail ja cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(encoder.encode(dados.senha())); // Armazena apenas o hash.
        usuario.setPerfil(PerfilUsuario.USER); // Cadastro público nunca define ADMIN.
        return UsuarioDto.de(repository.saveAndFlush(usuario));
    }

    public LoginResponse entrar(LoginRequest dados) {
        Usuario usuario = repository.findByEmailIgnoreCase(dados.email().trim())
                .orElse(null);
        // Mesmo erro para usuário inexistente e senha inválida.
        if (dados.senha().getBytes(StandardCharsets.UTF_8).length > 72 || usuario == null || !encoder.matches(dados.senha(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
        }
        if (usuario.getStatus() != StatusUsuario.ATIVO) {
            String code = usuario.getStatus() == StatusUsuario.PENDENTE ? "USUARIO_PENDENTE" : "USUARIO_BLOQUEADO";
            throw new ApiException(HttpStatus.FORBIDDEN, code, "Conta pendente de aprovacao ou bloqueada.");
        }
        return new LoginResponse(jwt.gerar(usuario), "Bearer", UsuarioDto.de(usuario));
    }
}
