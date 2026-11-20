package com.cti.provedor.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

/** Leitura e validação das datas de contratação da planilha. */
public final class Datas {
    private static final int ANO_INICIAL = 1990;
    private static final int ANO_FINAL = 2100;
    private static final List<DateTimeFormatter> FORMATOS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT)
    );

    private Datas() {}

    /** Devolve a data ou {@code null} para que a validação indique o erro na linha. */
    public static LocalDate lerContratacao(String texto) {
        if (texto == null) return null;
        String valor = texto.trim();
        for (DateTimeFormatter formato : FORMATOS) {
            try {
                return LocalDate.parse(valor, formato);
            } catch (DateTimeParseException ignored) {
                // Tenta o próximo formato aceito.
            }
        }
        return null;
    }

    static void conferirAnoContratacao(LocalDate data) {
        int ano = data.getYear();
        if (ano < ANO_INICIAL || ano > ANO_FINAL) {
            throw new IllegalArgumentException("A data deve estar entre " + ANO_INICIAL + " e " + ANO_FINAL + ".");
        }
    }
}
