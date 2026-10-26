package com.app.pro.repository;

import com.app.pro.entity.ConfiguracionDian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracionDianRepository extends JpaRepository<ConfiguracionDian,UUID>{

    Optional<ConfiguracionDian> FindByIdEmpresaTenant(String idEmpresaTenant);

}
