package com.cti.provedor.service;

/** Campos das linhas do upload, compartilhados pelo Java e pelo ETL Python. */
public final class Colunas {
    public static final String CODIGO = "codigoCliente";
    public static final String NOME = "nomeCliente";
    public static final String CONSULTOR = "consultor";
    public static final String SEGMENTO = "segmento";
    public static final String NIVEL = "nivelCliente";
    public static final String FATURAMENTO = "faturamentoAnual";
    public static final String SERVICOS = "servicosContratados";
    public static final String DATA = "dataContratacao";
    public static final String CIDADE = "cidade";
    public static final String UF = "uf";

    public static final int MAX_CODIGO = 60;
    public static final int MAX_NOME = 255;
    public static final int MAX_CONSULTOR = 120;
    public static final int MAX_SEGMENTO = 40;
    public static final int MAX_SERVICOS = 2000;
    public static final int MAX_NOME_SERVICO = 120;
    public static final int MAX_CIDADE = 120;

    private Colunas() {}
}
