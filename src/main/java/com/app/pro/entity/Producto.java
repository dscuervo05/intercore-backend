package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_producto;
    
    @Column(name = "codigo_barras_qr" , unique = true)
    private String codigoBarrasQR;
    
    private String nombre;
    private String descripcion;
    
    private BigDecimal precio_venta;
    private BigDecimal costo;
    
    @Column(name = "creado_en")
    private LocalDateTime credadoEn = LocalDateTime.now();
    
}
