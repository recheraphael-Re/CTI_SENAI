package com.cti.provedor.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cti.provedor.model.Servico;
public interface ServicoRepository extends JpaRepository<Servico, Long> {
}
