package com.cti.provedor.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cti.provedor.model.Contrato;
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    boolean existsByServicoId(Long idServico);
}
