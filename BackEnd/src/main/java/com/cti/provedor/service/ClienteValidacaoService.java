package com.cti.provedor.service;

import com.cti.provedor.dto.ClienteDTO;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ClienteValidacaoService {
    public List<ClienteDTO> validarEPreparar(List<ClienteDTO> clientes) {
        if (clientes == null || clientes.isEmpty()) throw new IllegalArgumentException("Envie pelo menos um cliente.");
        Set<String> codigos = new HashSet<>();
        for (ClienteDTO cliente : clientes) {
            if (cliente == null) throw new IllegalArgumentException("A lista não pode conter clientes nulos.");
            String codigo = cliente.codigoCliente().trim();
            if (!codigos.add(codigo.toUpperCase(Locale.ROOT))) throw new IllegalArgumentException("Código repetido na planilha: " + codigo);
            if (!Double.isFinite(cliente.faturamentoAnual())) throw new IllegalArgumentException("O faturamento deve ser um número finito.");
            Datas.conferirAnoContratacao(cliente.dataContratacao());
            if (Arrays.stream(cliente.servicosContratados().split(";", -1))
                    .anyMatch(s -> s.isBlank() || s.trim().length() > Colunas.MAX_NOME_SERVICO))
                throw new IllegalArgumentException("Cada serviço deve ter entre 1 e " + Colunas.MAX_NOME_SERVICO
                    + " caracteres; separe-os com ponto e vírgula.");
            if (Padronizacao.segmento(cliente.segmento()) == null)
                throw new IllegalArgumentException("Segmento não reconhecido: " + cliente.segmento());
        }
        return clientes.stream().map(this::padronizar).toList();
    }

    private String limpar(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }

    private ClienteDTO padronizar(ClienteDTO c) {
        String nome = Padronizacao.tituloNome(c.consultor());
        return new ClienteDTO(c.codigoCliente().trim(), limpar(c.nomeCliente()), nome,
            Padronizacao.segmento(c.segmento()), c.nivelCliente(), c.faturamentoAnual(),
            limpar(c.servicosContratados()), c.dataContratacao(), limpar(c.cidade()),
            limpar(c.uf()).toUpperCase(Locale.ROOT));
    }
}
