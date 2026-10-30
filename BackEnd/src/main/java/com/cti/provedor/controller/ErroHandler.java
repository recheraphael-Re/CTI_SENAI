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
    public ResponseEntity<Map<String,String>> conflito(DataIntegrityViolationException erro) {
        return ResponseEntity.status(409).body(Map.of("code", "CONFLITO_DADOS", "message", "Dados duplicados ou registros vinculados."));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String,String>> validacao(Exception erro) {
        return ResponseEntity.badRequest().body(Map.of("code", "VALIDACAO", "message", "Confira os campos enviados."));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> status(ResponseStatusException erro) {
        String code = erro instanceof ApiException api ? api.getCode() : switch (erro.getStatusCode().value()) {
            case 400 -> "VALIDACAO"; case 401 -> "NAO_AUTENTICADO"; case 403 -> "ACESSO_NEGADO"; case 404 -> "NAO_ENCONTRADO"; default -> "CONFLITO_DADOS";
        };
        return ResponseEntity.status(erro.getStatusCode()).body(Map.of("code", code, "message", erro.getReason() == null ? "Falha na operacao" : erro.getReason()));
    }
}
