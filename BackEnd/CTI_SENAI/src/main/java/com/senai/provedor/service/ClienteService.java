package com.senai.provedor.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.transaction.Transactional;
import com.senai.provedor.models.Cliente;
import com.senai.provedor.models.Consultor;
import com.senai.provedor.repository.ClienteRepository;
import com.senai.provedor.repository.ConsultorRepository;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final ConsultorRepository consultorRepository;

    public ClienteService(ClienteRepository clienteRepository, ConsultorRepository consultorRepository) {
        this.clienteRepository = clienteRepository;
        this.consultorRepository = consultorRepository;
    }

    private Consultor buscarConsultor(Long id) {
        return consultorRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Consultor nao encontrado"));
    }

    private void validar(Cliente cliente) {
        if ((cliente.getCodigoCTI() != null && cliente.getCodigoCTI().length() > 50)
                || (cliente.getFaixaFaturamento() != null && cliente.getFaixaFaturamento().length() > 100))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codigo CTI: ate 50 caracteres; faixa de faturamento: ate 100");
        if (cliente.getNomeEmpresa() == null || cliente.getNomeEmpresa().isBlank()
                || cliente.getNomeEmpresa().length() > 180)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome da empresa obrigatorio, ate 180 caracteres");
        if (cliente.getSegmento() == null || cliente.getSegmento().isBlank()
                || cliente.getSegmento().length() > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Segmento obrigatorio, ate 100 caracteres");
        if (cliente.getFaturamentoAnual() == null || cliente.getFaturamentoAnual().signum() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faturamento anual obrigatorio e nao negativo");
        if (cliente.getNivel() == null || cliente.getStatus() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nivel e status obrigatorios");
    }

    @Transactional
    public Cliente criar(Long idConsultor, Cliente cliente) {
        Consultor consultor = buscarConsultor(idConsultor);
        validar(cliente);
        // Copia os campos permitidos para impedir que um ID recebido atualize outro registro.
        Cliente novo = new Cliente(consultor, cliente.getNomeEmpresa(), cliente.getSegmento(),
            cliente.getFaturamentoAnual(), cliente.getNivel(), cliente.getStatus());
        novo.setCodigoCTI(cliente.getCodigoCTI());
        novo.setFaixaFaturamento(cliente.getFaixaFaturamento());
        return clienteRepository.save(novo);
    }

    public List<Cliente> listar() { return clienteRepository.findAll(); }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
    }

    @Transactional
    public Cliente atualizar(Long id, Long idConsultor, Cliente dados) {
        Cliente cliente = buscarPorId(id);
        Consultor consultor = buscarConsultor(idConsultor);
        validar(dados);
        cliente.setConsultor(consultor);
        cliente.setNomeEmpresa(dados.getNomeEmpresa());
        cliente.setSegmento(dados.getSegmento());
        cliente.setFaturamentoAnual(dados.getFaturamentoAnual());
        cliente.setNivel(dados.getNivel());
        cliente.setStatus(dados.getStatus());
        cliente.setCodigoCTI(dados.getCodigoCTI());
        cliente.setFaixaFaturamento(dados.getFaixaFaturamento());
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void excluir(Long id) { clienteRepository.delete(buscarPorId(id)); }
}
