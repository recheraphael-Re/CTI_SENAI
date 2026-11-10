package com.cti.provedor.service;

import com.cti.provedor.config.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest {
    JdbcTemplate jdbc;
    DashboardService service;
    @BeforeEach void preparar() {
        var ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + java.util.UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        new ResourceDatabasePopulator(new ClassPathResource("db/analytics.sql")).execute(ds);
        jdbc = new JdbcTemplate(ds);
        service = new DashboardService(jdbc, new ObjectMapper());
    }
    @Test void esquemaIsoladoNaoAlteraTabelasJPA() {
        jdbc.execute("CREATE TABLE public.cliente (id_cliente BIGINT PRIMARY KEY, nome_empresa VARCHAR(180))");
        jdbc.update("INSERT INTO public.cliente VALUES (1, 'Cadastro preservado')");
        assertEquals("Cadastro preservado", jdbc.queryForObject("SELECT nome_empresa FROM public.cliente", String.class));
        assertEquals(0, service.lotes().size());
        assertEquals(404, assertThrows(ApiException.class, () -> service.obter(null)).getStatusCode().value());
    }
    @Test void consultaUltimoLoteProcessadoEIgnoraFalhaPosterior() {
        jdbc.update("INSERT INTO cti_analytics.upload_lote (arquivo_nome,status) VALUES ('primeiro.xlsx','PROCESSADO')");
        jdbc.update("INSERT INTO cti_analytics.estatistica_lote (lote_id,resultado) VALUES (1, ?)", "{\"resumo\":{\"totalClientes\":3}}");
        jdbc.update("INSERT INTO cti_analytics.upload_lote (arquivo_nome,status) VALUES ('falhou.xlsx','ERRO')");
        var resposta = service.obter(null);
        assertEquals("primeiro.xlsx", ((java.util.Map<?,?>)resposta.get("lote")).get("arquivoNome"));
        assertEquals(2, service.lotes().size());
        var falhou = service.obter(2L);
        assertEquals("ERRO", ((java.util.Map<?,?>) falhou.get("lote")).get("status"));
        assertNull(falhou.get("estatisticas"));
        assertTrue(resposta.containsKey("geradoEm"));
        assertTrue(service.clientes(1).isEmpty());
    }
}
