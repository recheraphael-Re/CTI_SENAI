package com.cti.provedor.dto;

import com.cti.provedor.service.Colunas;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record ClienteDTO(
    @NotBlank(message = "O código do cliente é obrigatório") @Size(max = Colunas.MAX_CODIGO)
    @Pattern(regexp = "\\s*[A-Za-z0-9_-]+\\s*", message = "Use letras, números, hífen ou sublinhado no código") String codigoCliente,
    @NotBlank(message = "O nome do cliente é obrigatório") @Size(max = Colunas.MAX_NOME) String nomeCliente,
    @NotBlank(message = "O consultor é obrigatório") @Size(max = Colunas.MAX_CONSULTOR) String consultor,
    @NotBlank(message = "O segmento é obrigatório") @Size(max = Colunas.MAX_SEGMENTO) String segmento,
    @NotBlank(message = "O nível do cliente é obrigatório")
    @Pattern(regexp = "[ABC]", message = "O nível deve ser A, B ou C") String nivelCliente,
    @NotNull(message = "O faturamento anual é obrigatório")
    @PositiveOrZero(message = "O faturamento não pode ser negativo") @DecimalMax("99999999999999.99") Double faturamentoAnual,
    @NotBlank(message = "Informe os serviços contratados") @Size(max = Colunas.MAX_SERVICOS) String servicosContratados,
    @NotNull(message = "A data de contratação é obrigatória") LocalDate dataContratacao,
    @Size(max = Colunas.MAX_CIDADE) String cidade,
    @Pattern(regexp = "^$|^[A-Za-z]{2}$", message = "A UF deve possuir duas letras") String uf
) {}
