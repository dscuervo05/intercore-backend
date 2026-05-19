package com.app.pro.repository;

import com.app.pro.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, UUID> {
 
    List<Proveedor> findByIdEmpresaTenant(String idEmpresaTenante);
    
}
