package com.cti.provedor.controller;

import com.cti.provedor.dto.*;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.UsuarioRepository;
import com.cti.provedor.service.AuthService;
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
    public UsuarioDto cadastrar(@Valid @RequestBody CadastroRequest dados) {
        return service.cadastrar(dados); // Controller delega a regra ao Service.
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        return service.entrar(dados);
    }

    @GetMapping("/auth/me")
    public UsuarioDto me(Principal principal) {
        Usuario usuario = repository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return UsuarioDto.de(usuario);
    }

    @GetMapping("/publico")
    public Map<String, String> publico() { return Map.of("mensagem", "Rota pública"); }

    @GetMapping("/health")
    public Map<String, String> saude() { return Map.of("status", "ok"); }

    @GetMapping("/privado")
    public Map<String, String> privado() { return Map.of("mensagem", "JWT válido!"); }
}
