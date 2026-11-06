package com.cti.provedor.model;

import jakarta.persistence.*;

@Entity // Relaciona esta classe a uma entidade persistente.
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Identificador gerado pelo banco.
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 30)
    private String matricula;
    @Column(length = 60)
    private String turma;
    private java.time.Instant criadoEm;

    @PrePersist
    void registrarCriacao() { if (criadoEm == null) criadoEm = java.time.Instant.now(); }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getTurma() { return turma; }
    public void setTurma(String turma) { this.turma = turma; }
    public java.time.Instant getCriadoEm() { return criadoEm; }

    @Column(nullable = false)
    private String senhaHash; // Nunca armazenar a senha original.

    @Enumerated(EnumType.STRING) // Guarda USER/ADMIN como texto no PostgreSQL.
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil = PerfilUsuario.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusUsuario status = StatusUsuario.PENDENTE;

    public StatusUsuario getStatus() { return status; }
    public void setStatus(StatusUsuario status) { this.status = status; }

    public Usuario() {} // Construtor exigido pelo JPA.

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public PerfilUsuario getPerfil() { return perfil; }
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isAdmin() { return perfil == PerfilUsuario.ADMIN; }
}
