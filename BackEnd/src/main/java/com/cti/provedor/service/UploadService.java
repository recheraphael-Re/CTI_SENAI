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
        if (envio == null) throw new IllegalArgumentException("Confira os dados do upload.");
        var errosEnvio = validator.validate(envio);
        if (!errosEnvio.isEmpty()) throw new IllegalArgumentException("Confira os dados do upload.");
        if (envio.linhas().size() > maxLinhas)
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "LIMITE_UPLOAD", "Limite de linhas excedido: " + maxLinhas);
        Set<String> codigos = new HashSet<>();
        Set<Integer> numeros = new HashSet<>();
        List<LinhaPreparada> preparadas = new ArrayList<>();
        List<LinhaRejeitada> rejeitadas = new ArrayList<>();
        int posicao = 2;
        for (var linha : envio.linhas()) {
            int numero = linha.numeroLinha() == null ? posicao : linha.numeroLinha();
            posicao++;
            var cliente = linha.cliente();
            List<String> erros = new ArrayList<>();
            validator.validate(cliente).stream().sorted(Comparator.comparing(e -> e.getPropertyPath().toString()))
                .forEach(e -> erros.add(e.getPropertyPath() + ": " + e.getMessage()));
            if (numero <= 0 || !numeros.add(numero)) erros.add("Número da linha inválido ou repetido: " + numero);
            if (cliente.codigoCliente() != null && !codigos.add(cliente.codigoCliente().trim().toUpperCase(Locale.ROOT)))
                erros.add("Código repetido: " + cliente.codigoCliente().trim());
            if (erros.isEmpty()) {
                try { preparadas.add(new LinhaPreparada(numero, service.validarEPreparar(List.of(cliente)).get(0))); }
                catch (IllegalArgumentException e) { erros.add(e.getMessage()); }
            }
            if (!erros.isEmpty()) rejeitadas.add(new LinhaRejeitada(numero, numero, List.copyOf(erros)));
        }
        String nome = envio.arquivoNome().replace('\\', '/');
        nome = nome.substring(nome.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").trim();
        if (nome.isEmpty()) nome = "planilha";
        int descartadas = (envio.descartadas() == null ? 0 : envio.descartadas()) + rejeitadas.size();
        return new ResultadoUpload(nome, descartadas, preparadas.size(), List.copyOf(preparadas), List.copyOf(rejeitadas));
    }
}
