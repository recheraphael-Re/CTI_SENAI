package com.cti.provedor.service;

import com.cti.provedor.config.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private static final String LOTES = "SELECT id, arquivo_nome AS \"arquivoNome\", enviado_em AS \"enviadoEm\", "
        + "status, total_linhas AS \"totalLinhas\", linhas_validas AS \"linhasValidas\", "
        + "linhas_invalidas AS \"linhasInvalidas\" FROM cti_analytics.upload_lote ";
    public DashboardService(JdbcTemplate jdbc, ObjectMapper json) { this.jdbc = jdbc; this.json = json; }

    public List<Map<String, Object>> lotes() { return jdbc.queryForList(LOTES + "ORDER BY id DESC LIMIT 50"); }

    public Map<String, Object> obter(Long id) {
        var lotes = id == null
            ? jdbc.queryForList(LOTES + "WHERE status = 'PROCESSADO' ORDER BY id DESC LIMIT 1")
            : jdbc.queryForList(LOTES + "WHERE id = ?", id);
        if (lotes.isEmpty()) throw new ApiException(HttpStatus.NOT_FOUND, "LOTE_NAO_ENCONTRADO", "Nenhum lote processado encontrado.");
        var lote = lotes.getFirst();
        var contas = jdbc.queryForList("SELECT resultado FROM cti_analytics.estatistica_lote WHERE lote_id = ?", lote.get("id"));
        if (contas.isEmpty()) throw new ApiException(HttpStatus.CONFLICT, "LOTE_SEM_RESULTADO", "O lote ainda não tem análises disponíveis.");
        try {
            return Map.of("lote", lote, "estatisticas", json.readTree((String) contas.getFirst().get("resultado")));
        } catch (java.io.IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "ANALISE_INVALIDA", "As análises gravadas estão inválidas.");
        }
    }

    public List<Map<String, Object>> clientes(long id) {
        obter(id); // Confere a existência e a conclusão do lote.
        return jdbc.queryForList("""
            SELECT c.codigo_cti AS codigo, c.nome, co.nome AS consultor, s.nome AS segmento,
                c.nivel, c.faturamento_anual AS faturamento, c.cidade, c.uf,
                ct.data_contratacao AS "dataContratacao", se.nome AS servico
            FROM cti_analytics.cliente c
            JOIN cti_analytics.consultor co ON co.id = c.consultor_id
            JOIN cti_analytics.segmento s ON s.id = c.segmento_id
            JOIN cti_analytics.contrato ct ON ct.cliente_id = c.id
            JOIN cti_analytics.servico se ON se.id = ct.servico_id
            WHERE c.lote_id = ? ORDER BY c.codigo_cti, se.nome
            """, id);
    }
}
