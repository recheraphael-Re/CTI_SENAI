package com.senai.provedor.pratica;

import java.util.List;

public class MainPratica {
    public static void main(String[] args) {
        // A referencia tem o tipo da classe base; o objeto determina o metodo executado.
        List<Pessoa> pessoas = List.of(new Consultor("Daniel Vieira", "CON-0089"),
            new Cliente("Empresa Alpha", "CLI-001", "Industrial", "A"));
        for (Pessoa pessoa : pessoas) pessoa.exibirInformacoes();
        List<Servico> servicos = List.of(new ServicoDiagnostico("Diagnostico de Processos"),
            new ServicoConsultoria("Consultoria em Automacao"));
        for (Servico servico : servicos) {
            System.out.println("Servico: " + servico.getNome()); servico.executar();
        }
        List<Telemetria> registros = List.of(new TelemetriaSucesso("Execucao do servico", "Relatorio gerado"),
            new TelemetriaErro("Execucao do servico", "Falha ao acessar banco de dados"));
        for (Telemetria registro : registros) registro.exibirTelemetria();
    }
}
