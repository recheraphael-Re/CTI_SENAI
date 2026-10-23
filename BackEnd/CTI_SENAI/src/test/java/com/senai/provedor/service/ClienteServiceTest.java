package com.senai.provedor.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.senai.provedor.models.*;
import com.senai.provedor.repository.*;

class ClienteServiceTest {
    ClienteRepository clientes;
    ConsultorRepository consultores;
    ClienteService service;
    Consultor consultor;
    @BeforeEach void preparar() {
        clientes = mock(ClienteRepository.class);
        consultores = mock(ConsultorRepository.class);
        service = new ClienteService(clientes, consultores);
        consultor = new Consultor("Ana", "ana@example.com", "teste", "123");
        when(consultores.findById(1L)).thenReturn(Optional.of(consultor));
        when(clientes.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));
    }
    Cliente dados() { return new Cliente(null, "Alpha", "Industrial", new BigDecimal("1000.00"), NivelCliente.A, StatusCliente.ATIVO); }
    @Test void criaComConsultorPersistido() {
        Cliente entrada = dados(); entrada.setCodigoCTI("CLI-001"); entrada.setFaixaFaturamento("Ate 1 milhao");
        Cliente salvo = service.criar(1L, entrada);
        assertSame(consultor, salvo.getConsultor());
        assertNotSame(entrada, salvo);
        assertEquals("CLI-001",salvo.getCodigoCTI());
        assertEquals("Ate 1 milhao",salvo.getFaixaFaturamento());
        assertEquals("Alpha", salvo.getNomeEmpresa());
        verify(clientes).save(salvo);
    }
    @Test void rejeitaConsultorInexistente() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.criar(99L, dados())).getStatusCode().value());
        verify(clientes, never()).save(any());
    }
    @Test void rejeitaDadosInvalidos() {
        Cliente cliente = dados(); cliente.setFaturamentoAnual(new BigDecimal("-1"));
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.criar(1L, cliente)).getStatusCode().value());
        Cliente semNivel = dados(); semNivel.setNivel(null);
        assertThrows(ResponseStatusException.class, () -> service.criar(1L, semNivel));
        verify(clientes, never()).save(any());
    }
    @Test void atualizaRegistroExistente() {
        Cliente existente = dados(); when(clientes.findById(2L)).thenReturn(Optional.of(existente));
        Cliente novos = dados(); novos.setNomeEmpresa("Beta"); novos.setStatus(StatusCliente.INATIVO);
        assertSame(existente, service.atualizar(2L, 1L, novos));
        assertEquals("Beta", existente.getNomeEmpresa());
        assertEquals(StatusCliente.INATIVO, existente.getStatus());
        assertSame(consultor, existente.getConsultor());
    }
    @Test void listaBuscaEExclui() {
        Cliente cliente = dados(); when(clientes.findAll()).thenReturn(List.of(cliente));
        when(clientes.findById(2L)).thenReturn(Optional.of(cliente));
        assertEquals(List.of(cliente), service.listar());
        assertSame(cliente, service.buscarPorId(2L));
        service.excluir(2L); verify(clientes).delete(cliente);
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.buscarPorId(99L)).getStatusCode().value());
    }
}
