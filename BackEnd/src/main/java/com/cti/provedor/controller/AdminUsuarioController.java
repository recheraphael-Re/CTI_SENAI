package com.cti.provedor.controller;
import com.cti.provedor.dto.*;
import com.cti.provedor.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
@RestController
@RequestMapping({"/admin/usuarios", "/api/admin/usuarios"})
public class AdminUsuarioController {
    private final UsuarioService service;
    public AdminUsuarioController(UsuarioService service) { this.service = service; }
    @GetMapping public List<UsuarioDto> listar() { return service.listar(); }
    @PatchMapping("/{id}/status") public UsuarioDto alterar(@PathVariable Long id, @Valid @RequestBody StatusRequest dados, Principal principal) {
        return service.alterar(id, dados.status(), dados.perfil(), principal.getName());
    }
}
