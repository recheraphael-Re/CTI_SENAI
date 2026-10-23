package com.senai.provedor.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.senai.provedor.models.Telemetria;
import com.senai.provedor.service.IntegradorService;
@RestController @RequestMapping("/telemetrias")
public class TelemetriaController {
    private final IntegradorService service;
    public TelemetriaController(IntegradorService service) { this.service=service; }
    @GetMapping public List<Telemetria> listar(@RequestParam(required=false) Long idServico) {
        return service.listarTelemetrias(idServico);
    }
}
