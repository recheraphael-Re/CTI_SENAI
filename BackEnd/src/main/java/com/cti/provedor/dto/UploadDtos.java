package com.cti.provedor.dto;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public final class UploadDtos {
    private UploadDtos() {}
    public record EnvioValidadasRequest(
        @NotBlank @Size(max = 255) String arquivoNome,
        @PositiveOrZero @Max(1000000) Integer descartadas,
        @NotEmpty List<@NotNull LinhaEnvio> linhas) {}

    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = LinhaEnvioDeserializer.class)
    public record LinhaEnvio(@NotNull @Positive Integer numeroLinha,
        String codigoCliente, String nomeCliente, String consultor, String segmento,
        String nivelCliente, Double faturamentoAnual, String servicosContratados,
        LocalDate dataContratacao, String cidade, String uf) {
        public ClienteDTO cliente() {
            return new ClienteDTO(codigoCliente, nomeCliente, consultor, segmento,
                nivelCliente == null ? null : nivelCliente.trim().toUpperCase(java.util.Locale.ROOT),
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
}
