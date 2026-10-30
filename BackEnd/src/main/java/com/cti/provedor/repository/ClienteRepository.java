package com.cti.provedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente,Long> {
    
}
