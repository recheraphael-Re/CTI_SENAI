package com.cti.provedor.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record ClienteDTO(
    @NotBlank(message = "O código do cliente é obrigatório") @Size(max = 60)
    @Pattern(regexp = "\\s*[A-Za-z0-9_-]+\\s*", message = "Use letras, números, hífen ou sublinhado no código") String codigoCliente,
    @NotBlank(message = "O nome do cliente é obrigatório") @Size(max = 255) String nomeCliente,
    @NotBlank(message = "O consultor é obrigatório") @Size(max = 120) String consultor,
    @NotBlank(message = "O segmento é obrigatório") @Size(max = 40) String segmento,
    @NotBlank(message = "O nível do cliente é obrigatório")
    @Pattern(regexp = "[ABC]", message = "O nível deve ser A, B ou C") String nivelCliente,
    @NotNull(message = "O faturamento anual é obrigatório")
    @PositiveOrZero(message = "O faturamento não pode ser negativo") @DecimalMax("99999999999999.99") Double faturamentoAnual,
    @NotBlank(message = "Informe os serviços contratados") @Size(max = 2000) String servicosContratados,
    @NotNull(message = "A data de contratação é obrigatória") LocalDate dataContratacao,
    @Size(max = 120) String cidade,
    @Pattern(regexp = "^$|^[A-Za-z]{2}$", message = "A UF deve possuir duas letras") String uf
) {}
