package com.cti.provedor.config;

import com.cti.provedor.model.PerfilUsuario;
import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.UsuarioRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Comandos de manutenção executados pelo operador no terminal. */
public final class ContasCti {
    private static final String ADMIN = "admin@cti.com";
    private static final String USER = "user@cti.com";

    private ContasCti() {}

    public static boolean executar(String[] args) {
        if (args.length == 0) return false;
        boolean preparar = args[0].equals("preparar-contas-cti");
        if (!preparar && !args[0].equals("resetar-senha-admin-cti")) return false;
        if (args.length != 1) throw new IllegalArgumentException("Informe a senha pelo terminal.");

        char[] senha = lerSenha();
        try {
            validarSenha(senha);
            try (var contexto = new SpringApplicationBuilder(Configuracao.class)
                    .web(WebApplicationType.NONE)
                    .logStartupInfo(false)
                    .run("--spring.main.banner-mode=off", "--spring.jpa.hibernate.ddl-auto=none")) {
                var repository = contexto.getBean(UsuarioRepository.class);
                var transacao = new TransactionTemplate(contexto.getBean(PlatformTransactionManager.class));
                var contas = transacao.execute(status -> alterar(repository, preparar, senha));
                for (Usuario conta : contas) {
                    System.out.printf("Senha definida para %s (%s, %s).%n",
                            conta.getEmail(), conta.getPerfil(), conta.getStatus());
                }
            }
        } finally {
            Arrays.fill(senha, '\0');
        }
        return true;
    }

    static List<Usuario> alterar(UsuarioRepository repository, boolean preparar, char[] senha) {
        var admin = alterarConta(repository, ADMIN, "Administrador CTI", PerfilUsuario.ADMIN, preparar, senha);
        if (!preparar) return List.of(admin);
        var user = alterarConta(repository, USER, "Usuário CTI", PerfilUsuario.USER, true, senha);
        return List.of(admin, user);
    }

    private static Usuario alterarConta(UsuarioRepository repository, String email, String nome,
                                       PerfilUsuario perfil, boolean criar, char[] senha) {
        var conta = repository.findParaAlteracaoByEmailIgnoreCase(email).orElseGet(() -> {
            if (!criar) throw new IllegalStateException("Conta inexistente: " + email);
            var nova = new Usuario();
            nova.setEmail(email);
            nova.setNome(nome);
            nova.setPerfil(perfil);
            nova.setStatus(StatusUsuario.ATIVO);
            return nova;
        });
        if (conta.getPerfil() != perfil) throw new IllegalStateException("Perfil inesperado: " + email);
        conta.setSenhaHash(new BCryptPasswordEncoder().encode(CharBuffer.wrap(senha)));
        return repository.save(conta);
    }

    private static char[] lerSenha() {
        var console = System.console();
        if (console != null) {
            char[] entrada = console.readPassword("Nova senha: ");
            return entrada == null ? new char[0] : entrada;
        }
        try {
            String entrada = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).readLine();
            return entrada == null ? new char[0] : entrada.toCharArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao ler a senha.", ex);
        }
    }

    private static void validarSenha(char[] senha) {
        var valor = CharBuffer.wrap(senha);
        if (valor.toString().isBlank() || StandardCharsets.UTF_8.encode(valor).remaining() > 72) {
            throw new IllegalArgumentException("Informe uma senha de 1 a 72 bytes.");
        }
    }

    @ImportAutoConfiguration({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    @EntityScan(basePackageClasses = Usuario.class)
    @EnableJpaRepositories(basePackageClasses = UsuarioRepository.class)
    static class Configuracao {}
}
