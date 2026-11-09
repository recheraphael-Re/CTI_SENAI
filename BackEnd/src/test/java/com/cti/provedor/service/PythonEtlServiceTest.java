package com.cti.provedor.service;

import com.cti.provedor.config.ApiException;
import com.cti.provedor.dto.ClienteDTO;
import com.cti.provedor.dto.UploadDtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class PythonEtlServiceTest {
    HttpServer servidor;
    int status = 200;
    String resposta = "{\"ok\":true,\"loteId\":42,\"totalValidadas\":1}";
    ObjectMapper json = new ObjectMapper();
    String recebido;
    String chave;
    @BeforeEach void abrir() throws Exception {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/etl", req -> {
            recebido = new String(req.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            chave = req.getRequestHeaders().getFirst("X-ETL-Key");
            byte[] bytes = resposta.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            req.sendResponseHeaders(status, bytes.length);
            req.getResponseBody().write(bytes); req.close();
        });
        servidor.start();
    }
    @AfterEach void fechar() { servidor.stop(0); }
    PythonEtlService service() { return new PythonEtlService("http://127.0.0.1:" + servidor.getAddress().getPort() + "/etl", "teste", json); }
    ResultadoUpload lote() {
        return new ResultadoUpload("clientes.xlsx", 2, 1, List.of(new LinhaPreparada(5,
            new ClienteDTO("CTI1", "Empresa", "Ana Souza", "Indústria", "A", 100.0,
                "Internet; Firewall", LocalDate.of(2025,1,15), "Campinas", "SP"))));
    }
    @Test void enviaContratoPythonEConfirmaLote() throws Exception {
        assertEquals(42, service().enviar(lote()));
        var corpo = json.readTree(recebido);
        assertEquals(3, corpo.path("recebidas").asInt());
        assertEquals(2, corpo.path("invalidas").asInt());
        assertEquals("CTI1", corpo.path("linhas").get(0).path("codigo_cliente").asText());
        assertEquals("2025-01-15", corpo.path("linhas").get(0).path("data_contratacao").asText());
        assertEquals("teste", chave);
    }
    @Test void falhaPythonNuncaRetornaSucesso() {
        status = 503;
        assertEquals(502, assertThrows(ApiException.class, () -> service().enviar(lote())).getStatusCode().value());
        status = 422;
        assertEquals(422, assertThrows(ApiException.class, () -> service().enviar(lote())).getStatusCode().value());
        status = 200; resposta = "{\"ok\":true}";
        assertEquals(502, assertThrows(ApiException.class, () -> service().enviar(lote())).getStatusCode().value());
    }
}
