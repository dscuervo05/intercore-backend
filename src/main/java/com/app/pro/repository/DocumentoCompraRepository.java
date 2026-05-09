package com.app.pro.repository;

import com.app.pro.entity.DocumentoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DocumentoCompraRepository extends JpaRepository<DocumentoCompra, UUID>{
    
}
