package com.cti.provedor.dto;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public final class UploadDtos {
    private UploadDtos() {}
    public record EnvioValidadasRequest(
        @NotBlank @Size(max = 255) String arquivoNome,
        @NotNull @PositiveOrZero Integer descartadas,
        @NotEmpty List<@NotNull @Valid LinhaEnvio> linhas) {}

    public record LinhaEnvio(@NotNull @Positive Integer numeroLinha,
        String codigoCliente, String nomeCliente, String consultor, String segmento,
        String nivelCliente, Double faturamentoAnual, String servicosContratados,
        LocalDate dataContratacao, String cidade, String uf) {
        public ClienteDTO cliente() {
            return new ClienteDTO(codigoCliente, nomeCliente, consultor, segmento, nivelCliente,
                faturamentoAnual, servicosContratados, dataContratacao, cidade, uf);
        }
    }
    public record LinhaPreparada(Integer numeroLinha, ClienteDTO cliente) {}
    public record ResultadoUpload(String arquivoNome, Integer descartadas, int totalValidadas,
        List<LinhaPreparada> linhas) {}
}
