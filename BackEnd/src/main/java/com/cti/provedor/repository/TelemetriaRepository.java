package com.cti.provedor.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cti.provedor.model.Telemetria;
public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {
    java.util.List<Telemetria> findByServicoId(Long idServico);
    boolean existsByContratoId(Long idContrato);
}
