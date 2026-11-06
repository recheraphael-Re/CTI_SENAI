package com.cti.provedor.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record ClienteDTO(
    @NotBlank(message = "O código do cliente é obrigatório") String codigoCliente,
    @NotBlank(message = "O nome do cliente é obrigatório") String nomeCliente,
    @NotBlank(message = "O consultor é obrigatório") String consultor,
    @NotBlank(message = "O segmento é obrigatório") String segmento,
    @NotBlank(message = "O nível do cliente é obrigatório")
    @Pattern(regexp = "[ABC]", message = "O nível deve ser A, B ou C") String nivelCliente,
    @NotNull(message = "O faturamento anual é obrigatório")
    @Positive(message = "O faturamento deve ser maior que zero") Double faturamentoAnual,
    @NotBlank(message = "Informe os serviços contratados") String servicosContratados,
    @NotNull(message = "A data de contratação é obrigatória") LocalDate dataContratacao,
    String cidade,
    @Pattern(regexp = "^$|^[A-Za-z]{2}$", message = "A UF deve possuir duas letras") String uf
) {}
