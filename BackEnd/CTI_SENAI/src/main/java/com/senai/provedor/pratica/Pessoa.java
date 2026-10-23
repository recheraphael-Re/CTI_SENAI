package com.senai.provedor.pratica;

public class Pessoa {
    private final String nome;
    public Pessoa(String nome) { this.nome = nome; }
    public String getNome() { return nome; }
    public void exibirInformacoes() { System.out.println("Nome: " + nome); }
}
