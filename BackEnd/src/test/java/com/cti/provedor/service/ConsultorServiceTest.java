package com.cti.provedor.service;
import com.cti.provedor.model.Consultor;
import com.cti.provedor.repository.ConsultorRepository;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ConsultorServiceTest {
    ConsultorRepository repository = mock(ConsultorRepository.class);
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    ConsultorService service = new ConsultorService(repository, encoder);
    @Test void criaComHashSemAceitarIdEnviado() {
        Consultor entrada = new Consultor("Ana", "ANA@example.com", "senha123", "123"); entrada.setIdConsultor(99L);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        Consultor salvo = service.criar(entrada);
        assertNull(salvo.getIdConsultor()); assertEquals("ana@example.com", salvo.getEmail());
        assertTrue(encoder.matches("senha123", salvo.getSenha())); assertNotEquals(entrada.getSenha(), salvo.getSenha());
    }
    @Test void atualizaSemReenviarSenha() {
        Consultor existente = new Consultor("Ana", "ana@example.com", encoder.encode("senha123"), "123"); existente.setIdConsultor(1L);
        String hash = existente.getSenha(); when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.atualizar(1L, new Consultor("Ana Maria", "ana@example.com", null, "456"));
        assertEquals(hash, existente.getSenha()); assertEquals("Ana Maria", existente.getNome());
    }
    @Test void inexistenteRetorna404EDuplicado409() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.buscarPorId(99L)).getStatusCode().value());
        when(repository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(new Consultor()));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.criar(new Consultor("Ana", "ana@example.com", "senha123", "123"))).getStatusCode().value());
    }
}
