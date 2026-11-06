package com.cti.provedor.service;
import com.cti.provedor.dto.UploadDtos.*;
import com.cti.provedor.config.ApiException;
import jakarta.validation.Validation;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UploadLimiteTest {
    @Test void rejeitaExcessoAntesDeProcessarLinhas() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new UploadService(new ClienteValidacaoService(), factory.getValidator(), 1);
            var linha = new LinhaEnvio(2, null, null, null, null, null, null, null, null, null, null);
            var erro = assertThrows(ApiException.class, () -> service.validar(
                new EnvioValidadasRequest("clientes.xlsx", 0, List.of(linha, linha))));
            assertEquals(422, erro.getStatusCode().value());
        }
    }
}
