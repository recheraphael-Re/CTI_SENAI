package com.senai.provedor.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.provedor.models.Servico;
public interface ServicoRepository extends JpaRepository<Servico, Long> {
}
