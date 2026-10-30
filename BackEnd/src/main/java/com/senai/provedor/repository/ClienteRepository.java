package com.senai.provedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.senai.provedor.models.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente,Long> {
    
}
