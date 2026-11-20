package com.cti.provedor.dto;

import com.cti.provedor.service.Colunas;
import com.cti.provedor.service.Datas;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import jakarta.validation.constraints.*;

public final class UploadDtos {
    private UploadDtos() {}
    public record EnvioValidadasRequest(
        @NotBlank @Size(max = 255) String arquivoNome,
        @PositiveOrZero @Max(1000000) Integer descartadas,
        @NotEmpty List<@NotNull LinhaEnvio> linhas) {}

    @JsonDeserialize(using = LinhaEnvioDeserializer.class)
    public record LinhaEnvio(@NotNull @Positive Integer numeroLinha,
        String codigoCliente, String nomeCliente, String consultor, String segmento,
        String nivelCliente, Double faturamentoAnual, String servicosContratados,
        LocalDate dataContratacao, String cidade, String uf) {
        public ClienteDTO cliente() {
            return new ClienteDTO(codigoCliente, nomeCliente, consultor, segmento,
                nivelCliente == null ? null : nivelCliente.trim().toUpperCase(Locale.ROOT),
                faturamentoAnual, servicosContratados, dataContratacao, cidade, uf);
        }
    }
    public record LinhaPreparada(Integer numeroLinha, ClienteDTO cliente) {}
    public record LinhaRejeitada(Integer numero, Integer numeroLinha, List<String> erros) {}
    public record ResultadoUpload(String arquivoNome, Integer descartadas, int totalValidadas,
        List<LinhaPreparada> linhas, List<LinhaRejeitada> rejeitadas) {
        public ResultadoUpload(String arquivoNome, Integer descartadas, int totalValidadas, List<LinhaPreparada> linhas) {
            this(arquivoNome, descartadas, totalValidadas, linhas, List.of());
        }
    }

    /** Converte cada linha, deixando a validação reportar os campos rejeitados. */
    static final class LinhaEnvioDeserializer extends JsonDeserializer<LinhaEnvio> {
        @Override
        public LinhaEnvio deserialize(JsonParser parser, DeserializationContext contexto) throws IOException {
            JsonNode linha = parser.getCodec().readTree(parser);
            Integer numeroLinha = !linha.hasNonNull("numeroLinha") ? null
                : linha.path("numeroLinha").canConvertToInt() ? linha.path("numeroLinha").intValue() : -1;
            return new LinhaEnvio(numeroLinha, texto(linha, Colunas.CODIGO), texto(linha, Colunas.NOME),
                texto(linha, Colunas.CONSULTOR), texto(linha, Colunas.SEGMENTO), texto(linha, Colunas.NIVEL),
                numero(linha.get(Colunas.FATURAMENTO)), texto(linha, Colunas.SERVICOS),
                Datas.lerContratacao(texto(linha, Colunas.DATA)), texto(linha, Colunas.CIDADE), texto(linha, Colunas.UF));
        }

        private static String texto(JsonNode linha, String campo) {
            JsonNode valor = linha.get(campo);
            return valor == null || valor.isNull() || !valor.isValueNode() ? null : valor.asText();
        }

        private static Double numero(JsonNode valor) {
            if (valor == null || valor.isNull()) return null;
            if (valor.isNumber()) return valor.doubleValue();
            String texto = valor.asText().replace("R$", "").replaceAll("[\\s\\u00a0]", "");
            if (!texto.matches("-?[0-9.,]+")) return null;
            if (texto.contains(".") && texto.contains(",")) {
                texto = texto.lastIndexOf(',') > texto.lastIndexOf('.')
                    ? texto.replace(".", "").replace(',', '.') : texto.replace(",", "");
            } else if (texto.contains(",")) {
                texto = texto.replace(',', '.');
            } else if (texto.matches("-?\\d{1,3}(\\.\\d{3})+")) {
                texto = texto.replace(".", "");
            }
            try {
                return Double.valueOf(texto);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
