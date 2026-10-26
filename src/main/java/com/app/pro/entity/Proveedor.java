package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "proveedores")
@Data
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_proveedor; // 🎯 Identificador único del proveedor

    @Column(name = "id_empresa_tenant", nullable = false)
    private String idEmpresaTenant; // 🔐 Aislamiento multi-tenant

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

    // Relación correcta: Un proveedor tiene muchos documentos de compra
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<DocumentoCompra> documentoCompras;
}