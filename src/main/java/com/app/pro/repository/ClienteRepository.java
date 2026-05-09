package com.app.pro.repository;

import com.app.pro.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID>{
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
}
