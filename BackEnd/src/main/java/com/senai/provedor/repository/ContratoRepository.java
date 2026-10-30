package com.senai.provedor.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.provedor.models.Contrato;
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    boolean existsByServicoId(Long idServico);
}
