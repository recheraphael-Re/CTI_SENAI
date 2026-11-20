-- Histórico de importações.
SELECT id, arquivo_nome, enviado_em, status, total_linhas, linhas_validas, linhas_invalidas
FROM cti_analytics.upload_lote
ORDER BY id DESC;

-- Clientes e contratos no último lote processado.
SELECT c.codigo_cti, c.nome AS cliente, s.nome AS segmento, co.nome AS consultor,
       sv.nome AS servico, ct.data_contratacao, c.faturamento_anual
FROM cti_analytics.cliente c
JOIN cti_analytics.segmento s ON s.id = c.segmento_id
JOIN cti_analytics.consultor co ON co.id = c.consultor_id
JOIN cti_analytics.contrato ct ON ct.cliente_id = c.id
JOIN cti_analytics.servico sv ON sv.id = ct.servico_id
WHERE c.lote_id = (
    SELECT MAX(id) FROM cti_analytics.upload_lote WHERE status = 'PROCESSADO'
)
ORDER BY c.nome, sv.nome;

-- Indicadores salvos por lote.
SELECT lote_id, gerado_em, resultado::json -> 'resumo' AS indicadores
FROM cti_analytics.estatistica_lote
ORDER BY lote_id DESC;
