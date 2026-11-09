package com.cti.provedor.controller;
import com.cti.provedor.dto.UploadDtos.*;
import com.cti.provedor.service.UploadService;
import com.cti.provedor.service.PythonEtlService;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final UploadService service;
    private final PythonEtlService etl;
    public UploadController(UploadService service, PythonEtlService etl) { this.service = service; this.etl = etl; }
    @PostMapping("/validadas")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Map<String, Object> validar(@Valid @RequestBody EnvioValidadasRequest envio) {
        var validado = service.validar(envio);
        long loteId = etl.enviar(validado);
        return Map.of("loteId", loteId, "arquivoNome", validado.arquivoNome(),
            "descartadas", validado.descartadas(), "totalValidadas", validado.totalValidadas(),
            "linhas", validado.linhas(), "status", "PROCESSADO");
    }
}
