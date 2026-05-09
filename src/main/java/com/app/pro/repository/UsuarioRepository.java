package com.app.pro.repository;

import com.app.pro.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    // Aquí Spring Data JPA ya nos regala métodos como save(), findAll(), delete(), etc.
}