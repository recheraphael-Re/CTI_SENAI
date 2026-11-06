package com.cti.provedor;
import com.cti.provedor.config.AdminInicial;
import com.cti.provedor.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;

class AdminInicialTest {
    @Test void reinicioNaoAlteraContaExistente() throws Exception {
        var repository = mock(UsuarioRepository.class);
        var encoder = mock(PasswordEncoder.class);
        when(repository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(true);
        org.springframework.boot.CommandLineRunner runner = ReflectionTestUtils.invokeMethod(
            new AdminInicial(), "provisionarAdministradorInicial", repository, encoder, "admin@example.com", "senha123");
        runner.run();
        verify(repository).existsByEmailIgnoreCase("admin@example.com");
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(encoder);
    }
}
