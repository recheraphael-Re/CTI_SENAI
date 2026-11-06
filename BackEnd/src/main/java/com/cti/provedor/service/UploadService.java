package com.cti.provedor.service;
import com.cti.provedor.dto.UploadDtos.*;
import com.cti.provedor.config.ApiException;
import jakarta.validation.Validator;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

@Service
public class UploadService {
    private final ClienteValidacaoService service;
    private final Validator validator;
    private final int maxLinhas;
    public UploadService(ClienteValidacaoService service, Validator validator,
            @Value("${app.upload.max-linhas:5000}") int maxLinhas) {
        if (maxLinhas < 1) throw new IllegalArgumentException("Limite de linhas deve ser positivo");
        this.service = service; this.validator = validator; this.maxLinhas = maxLinhas;
    }
    public ResultadoUpload validar(EnvioValidadasRequest envio) {
        var errosEnvio = validator.validate(envio);
        if (!errosEnvio.isEmpty()) throw new IllegalArgumentException("Confira os dados do upload.");
        if (envio.linhas().size() > maxLinhas)
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "LIMITE_UPLOAD", "Limite de linhas excedido: " + maxLinhas);
        Set<String> codigos = new HashSet<>();
        Set<Integer> numeros = new HashSet<>();
        List<LinhaPreparada> preparadas = new ArrayList<>();
        for (var linha : envio.linhas()) {
            var cliente = linha.cliente();
            var erros = validator.validate(cliente);
            if (!erros.isEmpty()) {
                var erro = erros.stream().sorted(Comparator.comparing(e -> e.getPropertyPath().toString())).findFirst().orElseThrow();
                throw new IllegalArgumentException("Linha " + linha.numeroLinha() + " - " + erro.getPropertyPath() + ": " + erro.getMessage());
            }
            if (!numeros.add(linha.numeroLinha())) throw new IllegalArgumentException("Número de linha repetido: " + linha.numeroLinha());
            if (!codigos.add(cliente.codigoCliente().trim())) throw new IllegalArgumentException("Linha " + linha.numeroLinha() + ": código repetido: " + cliente.codigoCliente().trim());
            preparadas.add(new LinhaPreparada(linha.numeroLinha(), service.validarEPreparar(List.of(cliente)).get(0)));
        }
        return new ResultadoUpload(envio.arquivoNome().trim(), envio.descartadas(), preparadas.size(), preparadas);
    }
}
