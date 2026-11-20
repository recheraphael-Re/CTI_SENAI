package com.cti.provedor.config;
import com.cti.provedor.model.*;
import com.cti.provedor.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
@Configuration
public class AdminInicial {
    @Bean CommandLineRunner provisionarAdministradorInicial(UsuarioRepository repository, PasswordEncoder encoder,
        AppProperties properties) {
        var cadastro = properties.admin();
        String email = cadastro.email();
        String senha = cadastro.senha();
        return args -> {
            if (email.isBlank() && senha.isBlank()) return;
            if (email.isBlank() || senha.length() < 8 || senha.getBytes(StandardCharsets.UTF_8).length > 72)
                throw new IllegalArgumentException("Configure ADMIN_EMAIL e ADMIN_PASSWORD (8 caracteres a 72 bytes)");
            String normalizado = email.trim().toLowerCase(Locale.ROOT);
            if (repository.existsByEmailIgnoreCase(normalizado))
                return; // Não altera perfil, status ou senha de uma conta existente.
            Usuario admin = new Usuario(); admin.setNome(cadastro.nome()); admin.setEmail(normalizado);
            admin.setSenhaHash(encoder.encode(senha)); admin.setPerfil(PerfilUsuario.ADMIN); admin.setStatus(StatusUsuario.ATIVO);
            repository.saveAndFlush(admin);
        };
    }
}
