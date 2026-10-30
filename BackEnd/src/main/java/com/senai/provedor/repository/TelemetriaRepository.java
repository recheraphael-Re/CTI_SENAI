package com.senai.provedor.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.provedor.models.Telemetria;
public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {
    java.util.List<Telemetria> findByServicoId(Long idServico);
    boolean existsByContratoId(Long idContrato);
}
