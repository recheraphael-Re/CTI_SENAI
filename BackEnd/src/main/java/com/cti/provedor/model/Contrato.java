package com.cti.provedor.model;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="contrato")
public class Contrato {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_contrato")
    private Long id;
    public Long getId() { return id; }
    @Column(name="data_inicio", nullable=false)
    private LocalDate dataInicio;
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    @Column(nullable=false, length=30)
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @ManyToOne @JoinColumn(name="id_cliente", nullable=false)
    private Cliente cliente;
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    @ManyToOne @JoinColumn(name="id_servico", nullable=false)
    private Servico servico;
    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }
    @JsonIgnore @OneToMany(mappedBy="contrato")
    private List<Insight> insights = new ArrayList<>();
    public List<Insight> getInsights() { return insights; }
    public void setInsights(List<Insight> insights) { this.insights = insights; }
}
