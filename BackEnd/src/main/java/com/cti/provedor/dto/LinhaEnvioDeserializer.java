package com.cti.provedor.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.*;

/** Converte cada linha sem impedir o relatório de rejeições das demais linhas. */
public class LinhaEnvioDeserializer extends JsonDeserializer<UploadDtos.LinhaEnvio> {
    @Override public UploadDtos.LinhaEnvio deserialize(JsonParser parser, DeserializationContext contexto) throws IOException {
        JsonNode n = parser.getCodec().readTree(parser);
        Integer numero = !n.hasNonNull("numeroLinha") ? null
            : n.path("numeroLinha").canConvertToInt() ? n.path("numeroLinha").intValue() : -1;
        return new UploadDtos.LinhaEnvio(numero, texto(n,"codigoCliente"), texto(n,"nomeCliente"),
            texto(n,"consultor"), texto(n,"segmento"), texto(n,"nivelCliente"),
            numero(n.get("faturamentoAnual")), texto(n,"servicosContratados"),
            data(texto(n,"dataContratacao")), texto(n,"cidade"), texto(n,"uf"));
    }
    private static String texto(JsonNode n, String campo) {
        JsonNode valor = n.get(campo);
        return valor == null || valor.isNull() || !valor.isValueNode() ? null : valor.asText();
    }
    private static Double numero(JsonNode n) {
        if (n == null || n.isNull()) return null;
        if (n.isNumber()) return n.doubleValue();
        String t = n.asText().replace("R$", "").replaceAll("[\\s\\u00a0]", "");
        if (!t.matches("-?[0-9.,]+")) return null;
        if (t.contains(".") && t.contains(","))
            t = t.lastIndexOf(',') > t.lastIndexOf('.') ? t.replace(".", "").replace(',', '.') : t.replace(",", "");
        else if (t.contains(",")) t = t.replace(',', '.');
        else if (t.matches("-?\\d{1,3}(\\.\\d{3})+")) t = t.replace(".", "");
        try { return Double.valueOf(t); } catch (NumberFormatException e) { return null; }
    }
    private static LocalDate data(String texto) {
        if (texto == null) return null;
        for (DateTimeFormatter formato : new DateTimeFormatter[] {DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT)}) {
            try { return LocalDate.parse(texto.trim(), formato); } catch (DateTimeParseException ignored) {}
        }
        return null;
    }
}
