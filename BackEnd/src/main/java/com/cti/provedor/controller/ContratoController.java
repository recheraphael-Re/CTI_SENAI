package com.cti.provedor.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.cti.provedor.model.*;
import com.cti.provedor.service.IntegradorService;
@RestController @RequestMapping("/contratos")
public class ContratoController {
    private final IntegradorService service;
    public ContratoController(IntegradorService service) { this.service=service; }
    @GetMapping public List<Contrato> listar() { return service.listarContratos(); }
    @GetMapping("/{id}") public Contrato buscar(@PathVariable Long id) { return service.buscarContrato(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Contrato criar(@RequestParam Long idCliente, @RequestParam Long idServico, @RequestBody Contrato dados) { return service.salvarContrato(null, idCliente, idServico, dados); }
    @PutMapping("/{id}")
    public Contrato atualizar(@PathVariable Long id, @RequestParam Long idCliente, @RequestParam Long idServico, @RequestBody Contrato dados) { return service.salvarContrato(id, idCliente, idServico, dados); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) { service.excluirContrato(id); }
}
