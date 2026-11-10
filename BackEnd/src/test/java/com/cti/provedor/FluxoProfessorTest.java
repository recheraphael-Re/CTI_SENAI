package com.cti.provedor;

import com.cti.provedor.controller.*;
import com.cti.provedor.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.net.*;
import java.net.http.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class FluxoProfessorTest {
    @Test void planilhaVuePassaPeloJavaPythonEPersistenciaIsolada() throws Exception {
        Path raiz = Path.of("..").toAbsolutePath().normalize();
        Path python = raiz.resolve("analytics-python/.venv/Scripts/python.exe");
        if (!Files.exists(python)) python = raiz.resolve("analytics-python/.venv/bin/python");
        assumeTrue(Files.exists(python) && Files.exists(raiz.resolve("FrontEnd/node_modules"))
            && Files.exists(raiz.resolve("sample-data/dados.xlsx")), "Prepare Python, npm e a amostra para testar o fluxo completo");
        var exportar = new ProcessBuilder("node", "testes/exportar_amostra.mjs").directory(raiz.toFile())
            .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        String pedido = new String(exportar.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(exportar.waitFor(30, TimeUnit.SECONDS)); assertEquals(0, exportar.exitValue());
        int porta;
        try (var livre = new java.net.ServerSocket(0)) { porta = livre.getLocalPort(); }
        Path log = Files.createTempFile("cti-fluxo-python-", ".log");
        var processo = new ProcessBuilder(python.toString(), "testes/servidor_python_teste.py", String.valueOf(porta))
            .directory(raiz.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            var http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(java.time.Duration.ofSeconds(1)).build();
            String base = "http://127.0.0.1:" + porta;
            boolean pronto = false;
            for (int i=0; i<90 && processo.isAlive(); i++) {
                try {
                    pronto = http.send(HttpRequest.newBuilder(URI.create(base + "/saude"))
                        .timeout(java.time.Duration.ofSeconds(1)).build(), HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
                } catch (java.io.IOException ignored) {}
                if (pronto) break;
                Thread.sleep(500);
            }
            assertTrue(pronto, () -> "Python de teste não iniciou; consulte " + log);
            var json = new ObjectMapper();
            var validator = new LocalValidatorFactoryBean(); validator.afterPropertiesSet();
            try {
                var etl = new PythonEtlService(base + "/etl", "chave-fluxo-local", json);
                var mvc = MockMvcBuilders.standaloneSetup(new UploadController(
                    new UploadService(new ClienteValidacaoService(), validator, 5000), etl))
                    .setControllerAdvice(new ErroHandler()).build();
                mvc.perform(post("/api/uploads/validadas").contentType("application/json").content(pedido))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.linhasValidas").value(32))
                    .andExpect(jsonPath("$.linhasInvalidas").value(6)).andExpect(jsonPath("$.totalLinhas").value(38));
                var resposta = http.send(HttpRequest.newBuilder(URI.create(base + "/conferir-teste")).build(), HttpResponse.BodyHandlers.ofString());
                var dados = json.readTree(resposta.body());
                assertEquals(32, dados.path("cliente").asInt()); assertEquals(61, dados.path("contrato").asInt());
                assertEquals(6, dados.path("segmento").asInt()); assertEquals(4, dados.path("consultor").asInt());
                assertEquals(6, dados.path("servico").asInt());
                assertEquals(2261250.0, dados.path("estatisticas").path("resumo").path("media").asDouble());
                assertEquals(38, dados.path("lote").path("total_linhas").asInt());
            } finally { validator.close(); }
        } finally {
            var filhos = processo.descendants().toList();
            filhos.forEach(ProcessHandle::destroy);
            processo.destroy(); if (!processo.waitFor(5, TimeUnit.SECONDS)) processo.destroyForcibly();
            filhos.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
            try { if (!processo.isAlive()) Files.deleteIfExists(log); }
            catch (java.io.IOException ignored) { log.toFile().deleteOnExit(); }
        }
    }
}
