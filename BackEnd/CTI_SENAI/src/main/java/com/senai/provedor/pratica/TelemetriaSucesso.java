package com.senai.provedor.pratica;

public class TelemetriaSucesso extends Telemetria {
    private final String resultado;
    public TelemetriaSucesso(String evento, String resultado) { super(evento, "SUCESSO"); this.resultado = resultado; }
    @Override public void exibirTelemetria() {
        super.exibirTelemetria(); System.out.println("Resultado: " + resultado);
    }
}
