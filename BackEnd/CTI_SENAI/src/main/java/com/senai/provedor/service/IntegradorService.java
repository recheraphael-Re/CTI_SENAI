package com.senai.provedor.service;

import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.senai.provedor.models.*;
import com.senai.provedor.repository.*;

@Service
public class IntegradorService {
    private final ServicoRepository servicos;
    private final ContratoRepository contratos;
    private final ClienteRepository clientes;
    private final InsightRepository insights;
    private final TelemetriaRepository telemetrias;
    public IntegradorService(ServicoRepository servicos, ContratoRepository contratos,
            ClienteRepository clientes, InsightRepository insights, TelemetriaRepository telemetrias) {
        this.servicos=servicos; this.contratos=contratos; this.clientes=clientes;
        this.insights=insights; this.telemetrias=telemetrias;
    }
    private ResponseStatusException ausente(String nome) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, nome + " nao encontrado");
    }
    private void texto(String valor, int tamanho, String nome) {
        if (valor == null || valor.isBlank() || valor.length() > tamanho)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, nome + " obrigatorio; maximo " + tamanho + " caracteres");
    }
    public Servico buscarServico(Long id) { return servicos.findById(id).orElseThrow(() -> ausente("Servico")); }
    public Contrato buscarContrato(Long id) { return contratos.findById(id).orElseThrow(() -> ausente("Contrato")); }
    public Insight buscarInsight(Long id) { return insights.findById(id).orElseThrow(() -> ausente("Insight")); }
    public List<Servico> listarServicos() { return servicos.findAll(); }
    public List<Contrato> listarContratos() { return contratos.findAll(); }
    public List<Insight> listarInsights() { return insights.findAll(); }
    public List<Telemetria> listarTelemetrias(Long idServico) {
        if (idServico == null) return telemetrias.findAll();
        buscarServico(idServico); return telemetrias.findByServicoId(idServico);
    }
    @Transactional
    public Servico salvarServico(Long id, Servico dados) {
        texto(dados.getNome(), 150, "Nome"); texto(dados.getCategoria(),100,"Categoria");
        Servico alvo = id == null ? new Servico() : buscarServico(id);
        alvo.setNome(dados.getNome()); alvo.setCategoria(dados.getCategoria());
        return servicos.save(alvo);
    }
    @Transactional
    public Contrato salvarContrato(Long id, Long idCliente, Long idServico, Contrato dados) {
        texto(dados.getStatus(),30,"Status");
        if (dados.getDataInicio() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Data de inicio obrigatoria");
        Cliente cliente=clientes.findById(idCliente).orElseThrow(() -> ausente("Cliente"));
        Servico servico=buscarServico(idServico);
        Contrato alvo=id == null ? new Contrato() : buscarContrato(id);
        // Um contrato com historico mantem seus vinculos para preservar a consistencia dos insights.
        if (id != null && (!alvo.getCliente().getIdCliente().equals(idCliente)
                || !alvo.getServico().getId().equals(idServico)))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Os vinculos do contrato nao podem ser alterados");
        alvo.setCliente(cliente); alvo.setServico(servico);
        alvo.setDataInicio(dados.getDataInicio()); alvo.setStatus(dados.getStatus());
        return contratos.save(alvo);
    }
    @Transactional
    public Insight salvarInsight(Long id, Long idCliente, Long idContrato, Insight dados) {
        texto(dados.getTipo(),100,"Tipo"); texto(dados.getDescricao(),2000,"Descricao");
        Cliente cliente=clientes.findById(idCliente).orElseThrow(() -> ausente("Cliente"));
        Contrato contrato=idContrato == null ? null : buscarContrato(idContrato);
        if (contrato != null && !contrato.getCliente().getIdCliente().equals(idCliente))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Contrato pertence a outro cliente");
        Insight alvo=id == null ? new Insight() : buscarInsight(id);
        alvo.setTipo(dados.getTipo()); alvo.setDescricao(dados.getDescricao());
        alvo.setCliente(cliente); alvo.setContrato(contrato);
        if (id == null) alvo.setGeradoEm(LocalDateTime.now());
        return insights.save(alvo);
    }
    @Transactional public void excluirServico(Long id) {
        Servico alvo=buscarServico(id);
        if (contratos.existsByServicoId(id)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Servico possui contratos");
        servicos.delete(alvo);
    }
    @Transactional public void excluirContrato(Long id) {
        Contrato alvo=buscarContrato(id);
        if (insights.existsByContratoId(id) || telemetrias.existsByContratoId(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Contrato possui insights ou telemetrias");
        contratos.delete(alvo);
    }
    @Transactional public void excluirInsight(Long id) { insights.delete(buscarInsight(id)); }

    @Transactional
    public Telemetria executar(Long idServico, Long idContrato) {
        Servico servico=buscarServico(idServico);
        Contrato contrato=buscarContrato(idContrato);
        if (!contrato.getServico().getId().equals(idServico))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Contrato pertence a outro servico");
        Telemetria registro=new Telemetria();
        registro.setServico(servico); registro.setContrato(contrato);
        registro.setEvento("Execucao do servico"); registro.setTimestamp(LocalDateTime.now());
        try {
            if (!"ATIVO".equalsIgnoreCase(contrato.getStatus()))
                throw new IllegalStateException("Contrato deve estar ATIVO para executar");
            String categoria=Normalizer.normalize(servico.getCategoria(),Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(java.util.Locale.ROOT);
            String resultado=switch (categoria) {
                case "DIAGNOSTICO" -> "Diagnostico demonstrativo concluido para " + contrato.getCliente().getNomeEmpresa();
                case "CONSULTORIA" -> "Consultoria demonstrativa concluida para " + contrato.getCliente().getNomeEmpresa();
                default -> throw new IllegalStateException("Categoria sem executor: " + servico.getCategoria());
            };
            registro.setStatus("SUCESSO"); registro.setResultado(resultado);
        } catch (IllegalStateException erro) {
            registro.setStatus("ERRO"); registro.setMensagemErro(erro.getMessage());
        }
        // Falhas de negocio sao resultados persistidos; falhas do banco continuam sendo propagadas.
        return telemetrias.save(registro);
    }
}
