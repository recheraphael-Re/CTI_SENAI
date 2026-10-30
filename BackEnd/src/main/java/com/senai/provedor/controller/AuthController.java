package com.senai.provedor.controller;

import com.senai.provedor.dto.*;
import com.senai.provedor.models.Usuario;
import com.senai.provedor.repository.UsuarioRepository;
import com.senai.provedor.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({"", "/api"})
public class AuthController {
    private final AuthService service;
    private final UsuarioRepository repository;

    public AuthController(AuthService service, UsuarioRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping({"/auth/cadastro", "/auth/register"})
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest dados) {
        return service.cadastrar(dados); // Controller delega a regra ao Service.
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        return service.entrar(dados);
    }

    @GetMapping("/auth/me")
    public UsuarioResponse me(Principal principal) {
        Usuario usuario = repository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return UsuarioResponse.de(usuario);
    }

    @GetMapping("/publico")
    public Map<String, String> publico() { return Map.of("mensagem", "Rota pública"); }

    @GetMapping("/privado")
    public Map<String, String> privado() { return Map.of("mensagem", "JWT válido!"); }
}
