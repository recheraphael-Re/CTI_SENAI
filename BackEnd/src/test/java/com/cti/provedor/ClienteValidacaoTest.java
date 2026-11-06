package com.cti.provedor;
import com.cti.provedor.controller.*;
import com.cti.provedor.service.ClienteValidacaoService;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClienteValidacaoTest {
    MockMvc mvc;
    String cliente = """
        {"codigoCliente":" CTI001 ","nomeCliente":" Empresa ","consultor":" ANA SOUZA ",
         "segmento":"IND.","nivelCliente":"A","faturamentoAnual":100,
         "servicosContratados":" Internet ","dataContratacao":"2025-01-15","uf":"sp"}
        """;
    @BeforeEach void preparar() {
        var validator = new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new ClienteValidacaoController(new ClienteValidacaoService()),
                new UploadController(new com.cti.provedor.service.UploadService(new ClienteValidacaoService(), validator, 5000)))
            .setControllerAdvice(new ErroHandler()).build();
    }
    @Test void preparaSemPersistir() throws Exception {
        mvc.perform(post("/api/clientes/validar").contentType(MediaType.APPLICATION_JSON).content("[" + cliente + "]"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].codigoCliente").value("CTI001"))
            .andExpect(jsonPath("$[0].segmento").value("Indústria"))
            .andExpect(jsonPath("$[0].uf").value("SP"));
    }
    @Test void uploadPreservaOrigemEIdentificaLinhaInvalida() throws Exception {
        String linha = cliente.replace("{", "{\"numeroLinha\":15,");
        String envio = "{\"arquivoNome\":\"clientes.xlsx\",\"descartadas\":2,\"linhas\":[" + linha + "]}";
        mvc.perform(post("/api/uploads/validadas").contentType(MediaType.APPLICATION_JSON).content(envio))
            .andExpect(status().isOk()).andExpect(jsonPath("$.arquivoNome").value("clientes.xlsx"))
            .andExpect(jsonPath("$.descartadas").value(2)).andExpect(jsonPath("$.totalValidadas").value(1))
            .andExpect(jsonPath("$.linhas[0].numeroLinha").value(15))
            .andExpect(jsonPath("$.linhas[0].cliente.codigoCliente").value("CTI001"));
        mvc.perform(post("/api/uploads/validadas").contentType(MediaType.APPLICATION_JSON)
                .content(envio.replace(":100", ":0")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString("Linha 15")));
        mvc.perform(post("/api/uploads/validadas").contentType(MediaType.APPLICATION_JSON)
                .content(envio.replace("\"linhas\":[" + linha + "]", "\"linhas\":[]")))
            .andExpect(status().isBadRequest());
    }
    @Test void rejeitaCamposInvalidosEmItensDaLista() throws Exception {
        for (String invalido : new String[] {cliente.replace("\"A\"", "\"D\""),
                cliente.replace(":100", ":0"), cliente.replace(" Empresa ", " "),
                cliente.replace("2025-01-15", "2025-02-30"), cliente.replace("\"sp\"", "\"SPP\"")}) {
            mvc.perform(post("/api/clientes/validar").contentType(MediaType.APPLICATION_JSON).content("[" + invalido + "]"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").isNotEmpty());
        }
    }
    @Test void rejeitaLoteVazioNuloEDuplicado() throws Exception {
        for (String lote : new String[] {"[]", "[null]", "null", "[" + cliente + "," + cliente + "]"}) {
            mvc.perform(post("/api/clientes/validar").contentType(MediaType.APPLICATION_JSON).content(lote))
                .andExpect(status().isBadRequest());
        }
    }
}
