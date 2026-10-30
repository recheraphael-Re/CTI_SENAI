package com.senai.provedor.models;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="insight")
public class Insight {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_insight")
    private Long id;
    public Long getId() { return id; }
    @Column(nullable=false, length=100)
    private String tipo;
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    @Column(nullable=false, length=2000)
    private String descricao;
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    @Column(name="gerado_em", nullable=false)
    private LocalDateTime geradoEm;
    public LocalDateTime getGeradoEm() { return geradoEm; }
    public void setGeradoEm(LocalDateTime geradoEm) { this.geradoEm = geradoEm; }
    @ManyToOne @JoinColumn(name="id_cliente", nullable=false)
    private Cliente cliente;
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    @ManyToOne @JoinColumn(name="id_contrato")
    private Contrato contrato;
    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato contrato) { this.contrato = contrato; }
}
