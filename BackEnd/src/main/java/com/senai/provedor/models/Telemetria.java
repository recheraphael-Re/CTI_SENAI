package com.senai.provedor.models;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="telemetria")
public class Telemetria {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_telemetria")
    private Long id;
    public Long getId() { return id; }
    @Column(nullable=false, length=150)
    private String evento;
    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }
    @Column(nullable=false, length=30)
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @Column(nullable=false)
    private LocalDateTime timestamp;
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    @Column(length=2000)
    private String resultado;
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    @Column(name="mensagem_erro", length=2000)
    private String mensagemErro;
    public String getMensagemErro() { return mensagemErro; }
    public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }
    @ManyToOne @JoinColumn(name="id_servico", nullable=false)
    private Servico servico;
    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }
    @ManyToOne @JoinColumn(name="id_contrato", nullable=false)
    private Contrato contrato;
    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato contrato) { this.contrato = contrato; }
}
