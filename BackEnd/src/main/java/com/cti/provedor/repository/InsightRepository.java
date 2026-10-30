package com.cti.provedor.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cti.provedor.model.Insight;
public interface InsightRepository extends JpaRepository<Insight, Long> {
    boolean existsByContratoId(Long idContrato);
}
