package com.cti.provedor.controller;
import com.cti.provedor.dto.UploadDtos.*;
import com.cti.provedor.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final UploadService service;
    public UploadController(UploadService service) { this.service = service; }
    @PostMapping("/validadas")
    public ResultadoUpload validar(@Valid @RequestBody EnvioValidadasRequest envio) {
        return service.validar(envio);
    }
}
