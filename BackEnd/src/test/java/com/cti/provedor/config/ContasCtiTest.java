package com.cti.provedor.config;

import com.cti.provedor.model.PerfilUsuario;
import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop"})
class ContasCtiTest {
    @Autowired UsuarioRepository repository;

    @Test void preparaDuasContasEResetaApenasAdmin() {
        var encoder = new BCryptPasswordEncoder();
        var contas = ContasCti.alterar(repository, true, "12345".toCharArray());
        var admin = contas.get(0);
        var user = contas.get(1);

        assertEquals("admin@cti.com", admin.getEmail());
        assertEquals(PerfilUsuario.ADMIN, admin.getPerfil());
        assertEquals(StatusUsuario.ATIVO, admin.getStatus());
        assertEquals("user@cti.com", user.getEmail());
        assertEquals(PerfilUsuario.USER, user.getPerfil());
        assertEquals(StatusUsuario.ATIVO, user.getStatus());
        assertTrue(encoder.matches("12345", admin.getSenhaHash()));
        assertTrue(encoder.matches("12345", user.getSenhaHash()));
        assertNotEquals(admin.getSenhaHash(), user.getSenhaHash());

        String hashUser = user.getSenhaHash();
        var alteradas = ContasCti.alterar(repository, false, "nova-senha".toCharArray());
        assertEquals(1, alteradas.size());
        assertTrue(encoder.matches("nova-senha", admin.getSenhaHash()));
        assertEquals(hashUser, user.getSenhaHash());
    }

    @Test void resetExigeAdminExistenteComPerfilCorreto() {
        char[] senha = "nova-senha".toCharArray();
        assertThrows(IllegalStateException.class, () -> ContasCti.alterar(repository, false, senha));
        assertEquals(0, repository.count());

        var admin = ContasCti.alterar(repository, true, senha).get(0);
        admin.setPerfil(PerfilUsuario.USER);
        String hash = admin.getSenhaHash();
        assertThrows(IllegalStateException.class, () -> ContasCti.alterar(repository, false, senha));
        assertEquals(hash, admin.getSenhaHash());
    }
}
