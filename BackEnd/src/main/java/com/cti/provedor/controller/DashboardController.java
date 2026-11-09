package com.cti.provedor.controller;

import com.cti.provedor.service.DashboardService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }
    @GetMapping public Map<String, Object> obter(@RequestParam(required = false) Long loteId) { return service.obter(loteId); }
    @GetMapping("/lotes") public List<Map<String, Object>> lotes() { return service.lotes(); }
    @GetMapping("/lotes/{id}/clientes") public List<Map<String, Object>> clientes(@PathVariable long id) { return service.clientes(id); }
    @ExceptionHandler(org.springframework.jdbc.BadSqlGrammarException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> esquemaIndisponivel() {
        return Map.of("code", "ANALYTICS_SCHEMA", "message",
            "As tabelas de analytics não estão disponíveis. Confira a migração e o banco configurado.");
    }
}
