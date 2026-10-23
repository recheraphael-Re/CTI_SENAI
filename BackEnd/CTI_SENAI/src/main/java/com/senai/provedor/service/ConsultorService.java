package com.senai.provedor.service;

import com.senai.provedor.models.Consultor;
import com.senai.provedor.repository.ConsultorRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConsultorService {
    private final ConsultorRepository repository;
    private final PasswordEncoder encoder;
    public ConsultorService(ConsultorRepository repository, PasswordEncoder encoder) {
        this.repository = repository; this.encoder = encoder;
    }
    private void validar(Consultor dados) {
        if (dados.getNome() == null || dados.getNome().isBlank() || dados.getNome().length() > 150)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome obrigatorio; ate 150 caracteres");
        if (dados.getEmail() == null || dados.getEmail().isBlank() || dados.getEmail().length() > 150)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email obrigatorio; ate 150 caracteres");
        if (dados.getMatricula() != null && dados.getMatricula().length() > 50)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Matricula: ate 50 caracteres");
        if (dados.getTelefone() != null && dados.getTelefone().length() > 30)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Telefone: ate 30 caracteres");
    }
    private String hashSenha(String senha) {
        if (senha == null || senha.isBlank() || senha.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha obrigatoria; maximo de 72 bytes");
        return encoder.encode(senha);
    }
    private void copiar(Consultor alvo, Consultor dados) {
        alvo.setNome(dados.getNome().trim()); alvo.setEmail(dados.getEmail().trim().toLowerCase(Locale.ROOT));
        alvo.setTelefone(dados.getTelefone()); alvo.setMatricula(dados.getMatricula());
    }
    @Transactional public Consultor criar(Consultor dados) {
        validar(dados);
        String email = dados.getEmail().trim().toLowerCase(Locale.ROOT);
        if (repository.findByEmailIgnoreCase(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado");
        Consultor alvo = new Consultor(); copiar(alvo, dados); alvo.setSenha(hashSenha(dados.getSenha()));
        return repository.save(alvo);
    }
    public List<Consultor> listar() { return repository.findAll(); }
    public Consultor buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consultor nao encontrado"));
    }
    @Transactional public Consultor atualizar(Long id, Consultor dados) {
        validar(dados); Consultor alvo = buscarPorId(id);
        repository.findByEmailIgnoreCase(dados.getEmail().trim()).filter(outro -> !outro.getIdConsultor().equals(id))
            .ifPresent(outro -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado"); });
        copiar(alvo, dados);
        // Atualizar os dados cadastrais nao exige reenviar a senha.
        if (dados.getSenha() != null) alvo.setSenha(hashSenha(dados.getSenha()));
        return repository.save(alvo);
    }
    @Transactional public void excluir(Long id) { repository.delete(buscarPorId(id)); }
}
