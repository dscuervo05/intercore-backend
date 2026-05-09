package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_usuario;

    @Column(unique = true, nullable = false)
    private String nombre_usuario;

    private String contrasena_hash;
    
    @Column(unique = true)
    private String correo;

    private Boolean activo = true;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now();
}