package com.cti.provedor.controller;

import com.cti.provedor.dto.ClienteDTO;
import com.cti.provedor.service.ClienteValidacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteValidacaoController {
    private final ClienteValidacaoService service;
    public ClienteValidacaoController(ClienteValidacaoService service) { this.service = service; }

    @PostMapping("/validar")
    public List<ClienteDTO> validarClientes(
            @RequestBody @NotEmpty(message = "Envie pelo menos um cliente.")
            List<@NotNull(message = "O cliente não pode ser nulo.") @Valid ClienteDTO> clientes) {
        return service.validarEPreparar(clientes);
    }
}
