package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cotizaciones")
@Data
public class Cotizacion {
    
    // Columna SaaS
    @Column(name = "id_empresa_tenant", nullable = false)
    private String idEmpresaTenant;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idCotizacion;


    @Column(unique = true, nullable = false)
    private String consecutivo;

    private String estado;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision; // 👈 Ojo: camelCase aquí

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "total_final")
    private BigDecimal totalFinal; // 👈 Ojo: camelCase aquí

    // Relación con Cliente
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    // 🎯 LA RELACIÓN CLAVE PARA LA FASE 3 (CascadeType.ALL es vital)
    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL)
    private List<DetalleCotizacion> detalles; 
    
}