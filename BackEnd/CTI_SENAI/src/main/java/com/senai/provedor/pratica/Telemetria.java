package com.senai.provedor.pratica;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Telemetria {
    private final String evento, status;
    private final LocalDateTime timestamp;
    public Telemetria(String evento, String status) { this(evento, status, LocalDateTime.now()); }
    public Telemetria(String evento, String status, LocalDateTime timestamp) {
        this.evento = evento; this.status = status; this.timestamp = timestamp;
    }
    public void exibirTelemetria() {
        System.out.println("Evento: " + evento + "; status: " + status);
        System.out.println("Timestamp: " + timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
}
