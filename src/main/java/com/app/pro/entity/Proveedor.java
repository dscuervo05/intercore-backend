package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "proveedores")
@Data
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_proveedor;

    @Column(unique = true, nullable = false)
    private String nit;

    @Column(name = "nombre_razon_social", nullable = false)
    private String nombreRazonSocial;

    private String nombreContacto;
    private String telefono;
    private String correo;
    private String direccion;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now();
}