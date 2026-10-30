package com.senai.provedor.controller;
import com.senai.provedor.dto.*;
import com.senai.provedor.service.UsuarioAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
@RestController
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {
    private final UsuarioAdminService service;
    public UsuarioAdminController(UsuarioAdminService service) { this.service = service; }
    @GetMapping public List<UsuarioResponse> listar() { return service.listar(); }
    @PatchMapping("/{id}/status") public UsuarioResponse alterar(@PathVariable Long id, @Valid @RequestBody StatusRequest dados, Principal principal) {
        return service.alterar(id, dados.status(), dados.perfil(), principal.getName());
    }
}
