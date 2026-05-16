package com.app.pro.repository;

import com.app.pro.entity.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CotizacionRepository extends JpaRepository <Cotizacion, UUID>{
    
}
