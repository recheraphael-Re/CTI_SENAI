package com.cti.provedor.service;

import com.cti.provedor.dto.ClienteDTO;
import java.text.Normalizer;
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
            if (!codigos.add(codigo)) throw new IllegalArgumentException("Código repetido na planilha: " + codigo);
            if (!Double.isFinite(cliente.faturamentoAnual())) throw new IllegalArgumentException("O faturamento deve ser um número finito.");
        }
        return clientes.stream().map(this::padronizar).toList();
    }

    private String limpar(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }

    private String segmento(String texto) {
        String chave = Normalizer.normalize(limpar(texto), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        return switch (chave) {
            case "ind.", "industria" -> "Indústria";
            case "comercio" -> "Comércio";
            case "servicos" -> "Serviços";
            default -> limpar(texto);
        };
    }

    private ClienteDTO padronizar(ClienteDTO c) {
        String nome = limpar(c.consultor()).toLowerCase(Locale.forLanguageTag("pt-BR"));
        nome = nome.substring(0, 1).toUpperCase(Locale.forLanguageTag("pt-BR")) + nome.substring(1);
        return new ClienteDTO(c.codigoCliente().trim(), limpar(c.nomeCliente()), nome,
            segmento(c.segmento()), c.nivelCliente(), c.faturamentoAnual(),
            limpar(c.servicosContratados()), c.dataContratacao(), limpar(c.cidade()),
            limpar(c.uf()).toUpperCase(Locale.ROOT));
    }
}
