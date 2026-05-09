package com.app.pro.repository;

import com.app.pro.entity.DocumentoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DocumentoVentaRepository extends JpaRepository<DocumentoVenta, UUID>{
    
}
