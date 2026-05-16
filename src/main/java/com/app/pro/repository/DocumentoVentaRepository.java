package com.app.pro.repository;

import com.app.pro.entity.DocumentoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface DocumentoVentaRepository extends JpaRepository<DocumentoVenta, UUID>{
    
    @Query("SELECT SUM(v.totalFinal) FROM DocumentoVenta v")
    BigDecimal sumarIngresosTotales();
    
}
