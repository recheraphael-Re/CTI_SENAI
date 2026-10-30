package com.cti.provedor.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cti.provedor.model.Consultor;
public interface ConsultorRepository extends JpaRepository<Consultor, Long> {
    Optional<Consultor> findByEmailIgnoreCase(String email);
}
