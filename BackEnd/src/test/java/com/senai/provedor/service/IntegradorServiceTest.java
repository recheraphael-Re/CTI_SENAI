package com.senai.provedor.service;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.time.LocalDate;
import com.senai.provedor.models.*;
import com.senai.provedor.repository.*;

class IntegradorServiceTest {
    ServicoRepository servicos; ContratoRepository contratos; ClienteRepository clientes;
    InsightRepository insights; TelemetriaRepository telemetrias; IntegradorService service;
    Servico servico; Contrato contrato; Cliente cliente;
    @BeforeEach void preparar() {
        servicos=mock(ServicoRepository.class); contratos=mock(ContratoRepository.class);
        clientes=mock(ClienteRepository.class); insights=mock(InsightRepository.class);
        telemetrias=mock(TelemetriaRepository.class);
        service=new IntegradorService(servicos,contratos,clientes,insights,telemetrias);
        cliente=new Cliente(); cliente.setNomeEmpresa("Alpha"); ReflectionTestUtils.setField(cliente,"idCliente",1L);
        servico=new Servico(); servico.setNome("Processos"); servico.setCategoria("Diagnostico");
        ReflectionTestUtils.setField(servico,"id",2L);
        contrato=new Contrato(); contrato.setCliente(cliente); contrato.setServico(servico); contrato.setStatus("ATIVO");
        when(servicos.findById(2L)).thenReturn(Optional.of(servico));
        when(contratos.findById(3L)).thenReturn(Optional.of(contrato));
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(telemetrias.save(any())).thenAnswer(i -> i.getArgument(0));
        when(contratos.save(any())).thenAnswer(i -> i.getArgument(0));
        when(insights.save(any())).thenAnswer(i -> i.getArgument(0));
    }
    @Test void executaERegistraSucesso() {
        Telemetria registro=service.executar(2L,3L);
        assertEquals("SUCESSO",registro.getStatus()); assertNotNull(registro.getTimestamp());
        assertSame(contrato,registro.getContrato()); assertSame(servico,registro.getServico());
        assertTrue(registro.getResultado().contains("Alpha")); verify(telemetrias).save(registro);
        servico.setCategoria("Consultoria");
        assertTrue(service.executar(2L,3L).getResultado().startsWith("Consultoria"));
    }
    @Test void registraErroDeContratoInativo() {
        contrato.setStatus("ENCERRADO"); Telemetria registro=service.executar(2L,3L);
        assertEquals("ERRO",registro.getStatus()); assertNotNull(registro.getMensagemErro());
        verify(telemetrias).save(registro);
    }
    @Test void registraCategoriaSemExecutor() {
        servico.setCategoria("Outra"); assertEquals("ERRO",service.executar(2L,3L).getStatus());
    }
    @Test void rejeitaExecucaoComContratoDeOutroServico() {
        Servico outro=new Servico(); ReflectionTestUtils.setField(outro,"id",9L); contrato.setServico(outro);
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service.executar(2L,3L)).getStatusCode().value());
        verify(telemetrias,never()).save(any());
    }
    @Test void criaContratoComVinculosPersistidos() {
        Contrato dados=new Contrato(); dados.setStatus("ATIVO"); dados.setDataInicio(LocalDate.of(2026,10,5));
        Contrato salvo=service.salvarContrato(null,1L,2L,dados);
        assertSame(cliente,salvo.getCliente()); assertSame(servico,salvo.getServico());
        assertNotSame(dados,salvo);
        assertEquals(404,assertThrows(ResponseStatusException.class,()->service.salvarContrato(null,99L,2L,dados)).getStatusCode().value());
    }
    @Test void insightPreservaClienteDoContratoETimestamp() {
        Insight dados=new Insight(); dados.setTipo("Diagnostico"); dados.setDescricao("Melhorar processos");
        Insight salvo=service.salvarInsight(null,1L,3L,dados);
        assertSame(cliente,salvo.getCliente()); assertSame(contrato,salvo.getContrato()); assertNotNull(salvo.getGeradoEm());
        Cliente outro=new Cliente(); ReflectionTestUtils.setField(outro,"idCliente",9L); contrato.setCliente(outro);
        assertThrows(ResponseStatusException.class,()->service.salvarInsight(null,1L,3L,dados));
    }
    @Test void impedeExclusaoDoHistorico() {
        when(telemetrias.existsByContratoId(3L)).thenReturn(true);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.excluirContrato(3L)).getStatusCode().value());
        verify(contratos,never()).delete(any());
        when(contratos.existsByServicoId(2L)).thenReturn(true);
        assertThrows(ResponseStatusException.class,()->service.excluirServico(2L));
    }
}
