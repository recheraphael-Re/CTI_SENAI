package com.senai.provedor.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.provedor.models.Insight;
public interface InsightRepository extends JpaRepository<Insight, Long> {
    boolean existsByContratoId(Long idContrato);
}
