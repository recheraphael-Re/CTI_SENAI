package com.cti.provedor.repository;

import com.cti.provedor.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// O Spring Data JPA implementa os métodos de acesso ao banco.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u order by u.id")
    java.util.List<Usuario> buscarParaAdministracao();
}
