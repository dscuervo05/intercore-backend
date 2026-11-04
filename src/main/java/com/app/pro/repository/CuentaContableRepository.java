package com.app.pro.repository;

import com.app.pro.entity.CuentaContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaContableRepository extends JpaRepository<CuentaContable, Long> {

    @Query("SELECT c FROM CuentaContable c WHERE c.idEmpresaTenant = :idEmpresaTenant")
    List<CuentaContable> findByIdEmpresaTenant(@Param("idEmpresaTenant") String idEmpresaTenant);

    @Query("SELECT c FROM CuentaContable c WHERE c.codigo = :codigo AND c.idEmpresaTenant = :idEmpresaTenant")
    Optional<CuentaContable> findByCodigoAndIdEmpresaTenant(@Param("codigo") String codigo, @Param("idEmpresaTenant") String idEmpresaTenant);
}