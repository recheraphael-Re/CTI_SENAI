package com.cti.provedor.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configurações da aplicação definidas sob o prefixo {@code app}. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Admin admin, Upload upload, Etl etl) {
    public record Jwt(String secretBase64, long expirationMinutes) {
        @Override public String toString() {
            return "Jwt[expirationMinutes=" + expirationMinutes + "]";
        }
    }

    public record Cors(List<String> allowedOrigins) {}

    public record Admin(String nome, String email, String senha) {
        @Override public String toString() { return "Admin"; }
    }

    public record Upload(int maxLinhas) {}

    public record Etl(String url, String key) {
        @Override public String toString() { return "Etl"; }
    }
}
