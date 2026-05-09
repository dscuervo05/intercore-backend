package com.app.pro.repository;

import com.app.pro.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID>{
    
}
