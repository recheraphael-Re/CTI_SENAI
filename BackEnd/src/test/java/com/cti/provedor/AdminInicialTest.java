package com.cti.provedor;
import com.cti.provedor.config.AdminInicial;
import com.cti.provedor.config.AppProperties;
import com.cti.provedor.repository.UsuarioRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.*;

class AdminInicialTest {
    @Test void reinicioNaoAlteraContaExistente() throws Exception {
        var repository = mock(UsuarioRepository.class);
        var encoder = mock(PasswordEncoder.class);
        when(repository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(true);
        var properties = new Binder(new MapConfigurationPropertySource(Map.of(
            "app.admin.nome", "Administrador", "app.admin.email", "admin@example.com", "app.admin.senha", "senha123"
        ))).bind("app", AppProperties.class).get();
        org.springframework.boot.CommandLineRunner runner = ReflectionTestUtils.invokeMethod(
            new AdminInicial(), "provisionarAdministradorInicial", repository, encoder, properties);
        requireNonNull(runner).run();
        verify(repository).existsByEmailIgnoreCase("admin@example.com");
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(encoder);
    }
}
