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
            if (!codigos.add(codigo.toUpperCase(Locale.ROOT))) throw new IllegalArgumentException("Código repetido na planilha: " + codigo);
            if (!Double.isFinite(cliente.faturamentoAnual())) throw new IllegalArgumentException("O faturamento deve ser um número finito.");
            if (cliente.dataContratacao().getYear() < 1990 || cliente.dataContratacao().getYear() > 2100)
                throw new IllegalArgumentException("A data deve estar entre 1990 e 2100.");
            if (Arrays.stream(cliente.servicosContratados().split(";", -1)).anyMatch(s -> s.isBlank() || s.trim().length() > 120))
                throw new IllegalArgumentException("Cada serviço deve ter entre 1 e 120 caracteres; separe-os com ponto e vírgula.");
            if (!Set.of("Indústria", "Comércio", "Serviços", "Saúde", "Educação", "Tecnologia", "Varejo",
                    "Logística", "Construção", "Financeiro", "Agronegócio").contains(segmento(cliente.segmento())))
                throw new IllegalArgumentException("Segmento não reconhecido: " + cliente.segmento());
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
            case "ind", "ind.", "industria", "industrial" -> "Indústria";
            case "com", "com.", "comercio" -> "Comércio";
            case "serv", "serv.", "servico", "servicos" -> "Serviços";
            case "saude" -> "Saúde";
            case "edu", "educacao" -> "Educação";
            case "tec", "tech", "ti", "tecnologia" -> "Tecnologia";
            case "varejo" -> "Varejo";
            case "logistica" -> "Logística";
            case "construcao" -> "Construção";
            case "financeiro" -> "Financeiro";
            case "agronegocio" -> "Agronegócio";
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
