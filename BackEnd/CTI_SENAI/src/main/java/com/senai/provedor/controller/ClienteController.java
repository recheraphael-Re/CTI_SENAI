package com.senai.provedor.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.senai.provedor.models.Cliente;
import com.senai.provedor.service.ClienteService;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;
    public ClienteController(ClienteService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente criar(@RequestParam Long idConsultor, @RequestBody Cliente cliente) {
        return service.criar(idConsultor, cliente);
    }

    @GetMapping
    public List<Cliente> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Cliente buscar(@PathVariable Long id) { return service.buscarPorId(id); }

    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable Long id, @RequestParam Long idConsultor,
            @RequestBody Cliente cliente) {
        return service.atualizar(id, idConsultor, cliente);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) { service.excluir(id); }
}
