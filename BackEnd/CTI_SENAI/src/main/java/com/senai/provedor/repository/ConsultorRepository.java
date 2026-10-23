package com.senai.provedor.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.provedor.models.Consultor;
public interface ConsultorRepository extends JpaRepository<Consultor, Long> {
    Optional<Consultor> findByEmailIgnoreCase(String email);
}
