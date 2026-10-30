package com.cti.provedor.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

// Cria tabela para relacionar com o banco de dados
@Entity 
@Table (name="consultor") // cria tabela do consultor
public class Consultor {
@Id 
@GeneratedValue (strategy = GenerationType.IDENTITY)
@Column (name = "id_consultor")
private Long idConsultor;
@Column(name = "matricula", length = 50)
private String matricula;
public String getMatricula() { return matricula; }
public void setMatricula(String matricula) { this.matricula = matricula; }

// Cria coluna da tabela consultor

@Column (name= "nome",nullable = false, length = 150)
private  String nome;

@Column (name = "email", nullable = false, unique = true, length = 150)
private  String email;

@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
@Column (name = "senha", nullable = false, length = 150)
private  String senha;

@Column (name = "telefone", length = 30)
private  String telefone;

// Relacionamento
@com.fasterxml.jackson.annotation.JsonIgnore
@OneToMany (mappedBy = "consultor")
private List<Cliente> clientes = new ArrayList<>(); // Lista, pois o consultor pode ter varios clientes

// Cria o construtor cliente vazio, pois posso criar um cliente inicial sem passar parametros quando cria

public Consultor(){

}

// Cria outro construtor com parametros para iniciar

public Consultor(String nome, String email, String senha,String telefone){
    this.nome = nome;
    this.email = email;
    this.telefone = telefone;
    this.senha = senha;
}

public Long getIdConsultor(){
    return idConsultor;
}

public  void setIdConsultor(Long idConsultor){
    this.idConsultor = idConsultor;
}

public String getNome(){
    return  nome;
}

public void setNome(String nome){
    this.nome = nome;

}

public String getEmail(){
    return  email;
}

public void setSenha(String senha){
    this.senha = senha;
}
public String getSenha(){
    return senha;
}
public void setEmail(String email){
    this.email = email;
}

public String getTelefone(){
    return telefone;
}

public  void setTelefone(String telefone){
    this.telefone = telefone;
}

public List<Cliente> getClientes(){
    return clientes;
}

public  void setClientes(List<Cliente>clientes){
    this.clientes = clientes;
}
    
}
