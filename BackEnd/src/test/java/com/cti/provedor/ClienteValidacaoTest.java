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
    private com.cti.provedor.service.PythonEtlService etl() {
        var mock = org.mockito.Mockito.mock(com.cti.provedor.service.PythonEtlService.class);
        org.mockito.Mockito.when(mock.enviar(org.mockito.ArgumentMatchers.any())).thenReturn(7L);
        return mock;
    }
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
                new UploadController(new com.cti.provedor.service.UploadService(new ClienteValidacaoService(), validator, 5000), etl()))
            .setControllerAdvice(new ErroHandler()).build();
    }
    @Test void preparaSemPersistir() throws Exception {
        mvc.perform(post("/api/clientes/validar").contentType(MediaType.APPLICATION_JSON).content("[" + cliente + "]"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].codigoCliente").value("CTI001"))
            .andExpect(jsonPath("$[0].segmento").value("Indústria"))
            .andExpect(jsonPath("$[0].consultor").value("Ana Souza"))
            .andExpect(jsonPath("$[0].uf").value("SP"));
    }
    @Test void padronizaNomesComParticulasEPreservaSegmentosDoProjeto() throws Exception {
        for (String[] exemplo : new String[][] {
                {" MARIA   DA SILVA ", "Maria da Silva", "LOGÍSTICA", "Logística"},
                {" JOAO DOS SANTOS E ANA ", "Joao dos Santos e Ana", "TECH.", "Tecnologia"}}) {
            String envio = cliente.replace(" ANA SOUZA ", exemplo[0]).replace("IND.", exemplo[2]);
            mvc.perform(post("/api/clientes/validar").contentType(MediaType.APPLICATION_JSON).content("[" + envio + "]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].consultor").value(exemplo[1]))
                .andExpect(jsonPath("$[0].segmento").value(exemplo[3]));
        }
        mvc.perform(post("/api/clientes/validar").contentType(MediaType.APPLICATION_JSON)
                .content("[" + cliente.replace("IND.", "Segmento inexistente") + "]"))
            .andExpect(status().isBadRequest());
    }
    @Test void uploadMistoProcessaBoasEListaErrosSemGravarAsInvalidas() throws Exception {
        String boa = cliente.replace("{", "{\"numeroLinha\":2,");
        String repetida = cliente.replace("{", "{\"numeroLinha\":3,").replace("CTI001", "cti001");
        String nivel = cliente.replace("{", "{\"numeroLinha\":4,").replace("CTI001", "OUTRA").replace("\"A\"", "\"X\"");
        String data = cliente.replace("{", "{\"numeroLinha\":5,").replace("CTI001", "DATA").replace("2025-01-15", "2025-02-30");
        String valor = cliente.replace("{", "{\"numeroLinha\":6,").replace("CTI001", "VALOR").replace(":100", ":\"abc\"");
        String envio = "{\"arquivoNome\":\"teste.xlsx\",\"linhas\":[" + String.join(",", boa, repetida, nivel, data, valor) + "]}";
        mvc.perform(post("/api/uploads/validadas").contentType(MediaType.APPLICATION_JSON).content(envio))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.linhasValidas").value(1))
            .andExpect(jsonPath("$.linhasInvalidas").value(4)).andExpect(jsonPath("$.totalLinhas").value(5))
            .andExpect(jsonPath("$.rejeitadas.length()").value(4))
            .andExpect(jsonPath("$.rejeitadas[0].numeroLinha").value(3));
    }
    @Test void uploadAceitaNumeroBrasileiroDataBrasileiraENivelMinusculo() throws Exception {
        String linha = cliente.replace("{", "{\"numeroLinha\":2,")
            .replace(":100", ":\"R$ 1.850.000,50\"").replace("2025-01-15", "15/01/2025").replace("\"A\"", "\"b\"");
        mvc.perform(post("/api/uploads/validadas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"arquivoNome\":\"teste.xlsx\",\"linhas\":[" + linha + "]}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.linhas[0].cliente.nivelCliente").value("B"))
            .andExpect(jsonPath("$.linhas[0].cliente.faturamentoAnual").value(1850000.50));
    }
    @Test void uploadPreservaOrigemEIdentificaLinhaInvalida() throws Exception {
        String linha = cliente.replace("{", "{\"numeroLinha\":15,");
        String envio = "{\"arquivoNome\":\"clientes.xlsx\",\"descartadas\":2,\"linhas\":[" + linha + "]}";
        mvc.perform(post("/api/uploads/validadas").contentType(MediaType.APPLICATION_JSON).content(envio))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.loteId").value(7))
            .andExpect(jsonPath("$.arquivoNome").value("clientes.xlsx"))
            .andExpect(jsonPath("$.descartadas").value(2)).andExpect(jsonPath("$.totalValidadas").value(1))
            .andExpect(jsonPath("$.linhas[0].numeroLinha").value(15))
            .andExpect(jsonPath("$.linhas[0].cliente.codigoCliente").value("CTI001"));
        mvc.perform(post("/api/uploads/validadas").contentType(MediaType.APPLICATION_JSON)
                .content(envio.replace(":100", ":-1")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString("Linha 15")));
        mvc.perform(post("/api/uploads/validadas").contentType(MediaType.APPLICATION_JSON)
                .content(envio.replace("\"linhas\":[" + linha + "]", "\"linhas\":[]")))
            .andExpect(status().isBadRequest());
    }
    @Test void rejeitaCamposInvalidosEmItensDaLista() throws Exception {
        for (String invalido : new String[] {cliente.replace("\"A\"", "\"D\""),
                cliente.replace(":100", ":-1"), cliente.replace(" Empresa ", " "),
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
