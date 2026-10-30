package com.cti.provedor.model;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="servico")
public class Servico {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_servico")
    private Long id;
    public Long getId() { return id; }
    @Column(nullable=false, length=150)
    private String nome;
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    @Column(nullable=false, length=100)
    private String categoria;
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    @JsonIgnore @OneToMany(mappedBy="servico")
    private List<Contrato> contratos = new ArrayList<>();
    public List<Contrato> getContratos() { return contratos; }
    public void setContratos(List<Contrato> contratos) { this.contratos = contratos; }
    @JsonIgnore @OneToMany(mappedBy="servico")
    private List<Telemetria> telemetrias = new ArrayList<>();
    public List<Telemetria> getTelemetrias() { return telemetrias; }
    public void setTelemetrias(List<Telemetria> telemetrias) { this.telemetrias = telemetrias; }
}
