package com.senai.provedor.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.senai.provedor.models.*;
import com.senai.provedor.service.IntegradorService;
@RestController @RequestMapping("/servicos")
public class ServicoController {
    private final IntegradorService service;
    public ServicoController(IntegradorService service) { this.service=service; }
    @GetMapping public List<Servico> listar() { return service.listarServicos(); }
    @GetMapping("/{id}") public Servico buscar(@PathVariable Long id) { return service.buscarServico(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Servico criar(@RequestBody Servico dados) { return service.salvarServico(null, dados); }
    @PutMapping("/{id}")
    public Servico atualizar(@PathVariable Long id, @RequestBody Servico dados) { return service.salvarServico(id, dados); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) { service.excluirServico(id); }

    @PostMapping("/{id}/executar") @ResponseStatus(HttpStatus.CREATED)
    public Telemetria executar(@PathVariable Long id, @RequestParam Long idContrato) {
        return service.executar(id, idContrato);
    }
}
