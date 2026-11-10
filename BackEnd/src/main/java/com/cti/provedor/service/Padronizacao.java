package com.cti.provedor.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Padronização compartilhada pela validação Java, equivalente à limpeza no Python. */
public final class Padronizacao {
    private Padronizacao() {}

    // Inclui os segmentos e apelidos já aceitos neste projeto.
    private static final Map<String, String> SEGMENTOS = Map.ofEntries(
        Map.entry("ind", "Indústria"), Map.entry("industria", "Indústria"), Map.entry("industrial", "Indústria"),
        Map.entry("com", "Comércio"), Map.entry("comercio", "Comércio"),
        Map.entry("serv", "Serviços"), Map.entry("servico", "Serviços"), Map.entry("servicos", "Serviços"),
        Map.entry("saude", "Saúde"), Map.entry("edu", "Educação"), Map.entry("educacao", "Educação"),
        Map.entry("tec", "Tecnologia"), Map.entry("tech", "Tecnologia"),
        Map.entry("ti", "Tecnologia"), Map.entry("tecnologia", "Tecnologia"),
        Map.entry("varejo", "Varejo"), Map.entry("logistica", "Logística"),
        Map.entry("construcao", "Construção"), Map.entry("financeiro", "Financeiro"),
        Map.entry("agronegocio", "Agronegócio"));

    private static final Set<String> PARTICULAS = Set.of("de", "da", "do", "das", "dos", "e");

    static String simples(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    /** Retorna null para segmentos desconhecidos, que a validação deve rejeitar. */
    public static String segmento(String texto) {
        return SEGMENTOS.get(simples(texto));
    }

    /** ANA SOUZA → Ana Souza; MARIA DA SILVA → Maria da Silva. */
    public static String tituloNome(String texto) {
        if (texto == null || texto.isBlank()) return "";
        String[] palavras = texto.trim().toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder nome = new StringBuilder();
        for (int i = 0; i < palavras.length; i++) {
            String palavra = palavras[i];
            if (i > 0) nome.append(' ');
            if (i > 0 && PARTICULAS.contains(palavra)) nome.append(palavra);
            else nome.append(palavra.substring(0, 1).toUpperCase(Locale.ROOT)).append(palavra.substring(1));
        }
        return nome.toString();
    }
}
