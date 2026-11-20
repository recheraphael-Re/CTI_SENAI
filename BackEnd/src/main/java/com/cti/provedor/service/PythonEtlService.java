package com.cti.provedor.service;

import com.cti.provedor.config.ApiException;
import com.cti.provedor.config.AppProperties;
import com.cti.provedor.dto.UploadDtos.ResultadoUpload;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PythonEtlService {
    private final URI url;
    private final String chave;
    private final ObjectMapper json;
    private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(5)).build();

    @Autowired
    public PythonEtlService(AppProperties properties, ObjectMapper json) {
        this(properties.etl().url(), properties.etl().key(), json);
    }

    public PythonEtlService(String url, String chave, ObjectMapper json) {
        this.url = URI.create(url); this.chave = chave; this.json = json;
    }

    public long enviar(ResultadoUpload upload) {
        if (chave.isBlank()) throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
            "ETL_CONFIG", "Configure ETL_API_KEY no backend e no Python.");
        var linhas = upload.linhas().stream().map(l -> {
            var c = l.cliente();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("linha", l.numeroLinha());
            m.put(Colunas.CODIGO, c.codigoCliente());
            m.put(Colunas.NOME, c.nomeCliente());
            m.put(Colunas.CONSULTOR, c.consultor());
            m.put(Colunas.SEGMENTO, c.segmento());
            m.put(Colunas.NIVEL, c.nivelCliente());
            m.put(Colunas.FATURAMENTO, c.faturamentoAnual());
            m.put(Colunas.SERVICOS, c.servicosContratados());
            m.put(Colunas.DATA, c.dataContratacao().toString());
            m.put(Colunas.CIDADE, c.cidade());
            m.put(Colunas.UF, c.uf());
            return m;
        }).toList();
        try {
            var corpo = Map.of("arquivoNome", upload.arquivoNome(), "recebidas",
                upload.totalValidadas() + upload.descartadas(), "invalidas", upload.descartadas(),
                "comAviso", 0, "linhas", linhas);
            var req = HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(90))
                .header("Content-Type", "application/json").header("X-ETL-Key", chave)
                .POST(HttpRequest.BodyPublishers.ofByteArray(json.writeValueAsBytes(corpo))).build();
            var resposta = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() == 422) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                "ETL_VALIDACAO", "O Python recusou os dados. Confira as regras da importação.");
            if (resposta.statusCode() != 200) throw new ApiException(HttpStatus.BAD_GATEWAY,
                "ETL_FALHA", "O processamento Python falhou. Confira a configuração e o log do serviço.");
            var retorno = json.readTree(resposta.body());
            if (!retorno.path("ok").asBoolean() || !retorno.path("loteId").canConvertToLong()
                    || retorno.path("loteId").asLong() <= 0
                    || retorno.path("totalValidadas").asInt(-1) != upload.totalValidadas())
                throw new ApiException(HttpStatus.BAD_GATEWAY, "ETL_RESPOSTA", "Resposta inválida do serviço Python.");
            return retorno.path("loteId").asLong();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "ETL_INTERROMPIDO", "Processamento interrompido. Consulte o histórico antes de reenviar.");
        } catch (java.io.IOException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "ETL_INDISPONIVEL",
                "Sem resposta do Python. Confira se o serviço está ligado e consulte o histórico antes de reenviar.");
        }
    }

}
