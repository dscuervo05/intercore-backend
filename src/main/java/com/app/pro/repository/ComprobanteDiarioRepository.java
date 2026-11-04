package com.app.pro.repository;

import com.app.pro.entity.ComprobanteDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import  org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComprobanteDiarioRepository extends JpaRepository<ComprobanteDiario, Long> {

    List<ComprobanteDiario> findByIdEmpresaTenant(String idEmpresaTenant);

}
