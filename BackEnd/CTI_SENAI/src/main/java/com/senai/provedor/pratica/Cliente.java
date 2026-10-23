package com.senai.provedor.pratica;

public class Cliente extends Pessoa {
    private final String codigoCTI, segmento, nivel;
    public Cliente(String nome, String codigoCTI, String segmento, String nivel) {
        super(nome); this.codigoCTI = codigoCTI; this.segmento = segmento; this.nivel = nivel;
    }
    @Override public void exibirInformacoes() {
        super.exibirInformacoes();
        System.out.println("Codigo CTI: " + codigoCTI + "; segmento: " + segmento + "; nivel: " + nivel);
    }
}
