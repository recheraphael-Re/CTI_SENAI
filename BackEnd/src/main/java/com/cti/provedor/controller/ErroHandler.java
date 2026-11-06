package com.cti.provedor.controller;
import com.cti.provedor.config.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
@RestControllerAdvice
public class ErroHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,Object>> conflito(DataIntegrityViolationException erro) {
        return ResponseEntity.status(409).body(Map.of("code", "CONFLITO_DADOS", "message", "Dados duplicados ou registros vinculados."));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> campos(MethodArgumentNotValidException erro) {
        Map<String, String> campos = new java.util.LinkedHashMap<>();
        erro.getBindingResult().getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        var primeiro = campos.entrySet().stream().findFirst();
        String mensagem = primeiro.map(Map.Entry::getValue).orElse("Confira os campos enviados.");
        return ResponseEntity.badRequest().body(Map.of("code", "VALIDACAO", "message", mensagem,
            "erro", mensagem, "campo", primeiro.map(Map.Entry::getKey).orElse("corpo"), "campos", campos));
    }
    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
    public ResponseEntity<Map<String,Object>> lote(org.springframework.web.method.annotation.HandlerMethodValidationException erro) {
        for (var resultado : erro.getAllValidationResults()) {
            if (resultado instanceof org.springframework.validation.Errors campos && campos.hasFieldErrors()) {
                var campo = campos.getFieldErrors().get(0);
                return dadosInvalidos(campo.getField(), campo.getDefaultMessage());
            }
            if (!resultado.getResolvableErrors().isEmpty()) {
                return dadosInvalidos("clientes", resultado.getResolvableErrors().get(0).getDefaultMessage());
            }
        }
        return dadosInvalidos("clientes", "Confira os clientes enviados.");
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> regra(IllegalArgumentException erro) {
        return dadosInvalidos("clientes", erro.getMessage());
    }
    private ResponseEntity<Map<String,Object>> dadosInvalidos(String campo, String mensagem) {
        return ResponseEntity.badRequest().body(Map.of("code", "VALIDACAO", "message", mensagem,
            "mensagem", "Dados inválidos", "campo", campo, "erro", mensagem));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,Object>> validacao(Exception erro) {
        return dadosInvalidos("corpo", "JSON inválido. Confira os tipos dos campos e use datas válidas no formato AAAA-MM-DD.");
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,Object>> status(ResponseStatusException erro) {
        String code = erro instanceof ApiException api ? api.getCode() : switch (erro.getStatusCode().value()) {
            case 400 -> "VALIDACAO"; case 401 -> "NAO_AUTENTICADO"; case 403 -> "ACESSO_NEGADO"; case 404 -> "NAO_ENCONTRADO"; default -> "CONFLITO_DADOS";
        };
        return ResponseEntity.status(erro.getStatusCode()).body(Map.of("code", code, "message", erro.getReason() == null ? "Falha na operacao" : erro.getReason()));
    }
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String,Object>> parametro(org.springframework.web.bind.MissingServletRequestParameterException erro) {
        return dadosInvalidos(erro.getParameterName(), "Parametro obrigatorio ausente: " + erro.getParameterName());
    }
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String,Object>> tipo(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException erro) {
        return dadosInvalidos(erro.getName(), "Valor invalido para o parametro: " + erro.getName());
    }
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String,Object>> metodo(org.springframework.web.HttpRequestMethodNotSupportedException erro) {
        var headers = new HttpHeaders();
        if (erro.getSupportedHttpMethods() != null) headers.setAllow(erro.getSupportedHttpMethods());
        return ResponseEntity.status(405).headers(headers).body(Map.of("code", "METODO_NAO_PERMITIDO", "message", "Metodo nao permitido."));
    }
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Map<String,Object>> rota(org.springframework.web.servlet.resource.NoResourceFoundException erro) {
        return ResponseEntity.status(404).body(Map.of("code", "NAO_ENCONTRADO", "message", "Recurso nao encontrado."));
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String,Object>> acesso(org.springframework.security.access.AccessDeniedException erro) {
        return ResponseEntity.status(403).body(Map.of("code", "ACESSO_NEGADO", "message", "Acesso negado."));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> inesperado(Exception erro) {
        org.slf4j.LoggerFactory.getLogger(ErroHandler.class).error("Erro inesperado", erro);
        return ResponseEntity.status(500).body(Map.of("code", "ERRO_INTERNO", "message", "Erro interno. Tente novamente em instantes."));
    }

}
