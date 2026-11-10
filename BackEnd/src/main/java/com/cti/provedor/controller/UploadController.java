package com.cti.provedor.controller;
import com.cti.provedor.dto.UploadDtos.*;
import com.cti.provedor.service.UploadService;
import com.cti.provedor.service.PythonEtlService;
import java.util.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final UploadService service;
    private final PythonEtlService etl;
    public UploadController(UploadService service, PythonEtlService etl) { this.service = service; this.etl = etl; }
    @PostMapping("/validadas")
    public ResponseEntity<Map<String, Object>> validar(@Valid @RequestBody EnvioValidadasRequest envio) {
        var validado = service.validar(envio);
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("arquivoNome", validado.arquivoNome());
        resposta.put("descartadas", validado.descartadas());
        resposta.put("totalValidadas", validado.totalValidadas());
        resposta.put("linhasValidas", validado.totalValidadas());
        resposta.put("linhasInvalidas", validado.descartadas());
        resposta.put("totalLinhas", validado.totalValidadas() + validado.descartadas());
        resposta.put("rejeitadas", validado.rejeitadas());
        resposta.put("linhas", validado.linhas());
        if (validado.totalValidadas() == 0) {
            String erro = "Nenhuma linha válida para processar.";
            if (!validado.rejeitadas().isEmpty()) erro += " Linha " + validado.rejeitadas().getFirst().numero() + ": "
                + String.join("; ", validado.rejeitadas().getFirst().erros());
            resposta.put("status", "REJEITADO"); resposta.put("erro", erro); resposta.put("message", erro);
            return ResponseEntity.badRequest().body(resposta);
        }
        resposta.put("loteId", etl.enviar(validado));
        resposta.put("status", "PROCESSADO");
        return ResponseEntity.status(201).body(resposta);
    }
}
