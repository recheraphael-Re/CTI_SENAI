package com.cti.provedor.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.cti.provedor.model.*;
import com.cti.provedor.service.IntegradorService;
@RestController @RequestMapping("/insights")
public class InsightController {
    private final IntegradorService service;
    public InsightController(IntegradorService service) { this.service=service; }
    @GetMapping public List<Insight> listar() { return service.listarInsights(); }
    @GetMapping("/{id}") public Insight buscar(@PathVariable Long id) { return service.buscarInsight(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Insight criar(@RequestParam Long idCliente, @RequestParam(required=false) Long idContrato, @RequestBody Insight dados) { return service.salvarInsight(null, idCliente, idContrato, dados); }
    @PutMapping("/{id}")
    public Insight atualizar(@PathVariable Long id, @RequestParam Long idCliente, @RequestParam(required=false) Long idContrato, @RequestBody Insight dados) { return service.salvarInsight(id, idCliente, idContrato, dados); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) { service.excluirInsight(id); }
}
