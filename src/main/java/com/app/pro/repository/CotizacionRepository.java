package com.app.pro.repository;

import com.app.pro.entity.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface CotizacionRepository extends JpaRepository <Cotizacion, UUID>{
    
    List<Cotizacion> findByIdEmpresaTenant(String idEmpresaTenante);
    
}
