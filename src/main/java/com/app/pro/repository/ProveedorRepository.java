package com.app.pro.repository;

import com.app.pro.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProveedorRepository extends JpaRepository<Proveedor, UUID> {
    
}
