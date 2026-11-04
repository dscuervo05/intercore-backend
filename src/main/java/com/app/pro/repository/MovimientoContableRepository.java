package com.app.pro.repository;

import com.app.pro.entity.MovimientoContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoContableRepository extends JpaRepository<MovimientoContable, Long>{
    
    List<MovimientoContable> findByIdEmpresaTenant(String idEmpresaTenant);
    
    // Útil para buscar todos los débitos y créditos de un asiento en particular
    List<MovimientoContable> findByComprobanteId(Long comprobanteId);
    
}
