package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "detalle_venta")
@Data

public class DetalleVenta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_detalle;
    
    @ManyToOne
    @JoinColumn(name = "id_documento", nullable = false)
    @JsonBackReference
    private DocumentoVenta documentoVenta;
    
    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false )
    private Producto producto;
    
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotalBruto;
    
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
    private BigDecimal valorDescuento = BigDecimal.ZERO;
    
    private BigDecimal baseImponible = BigDecimal.ZERO;
    
    private BigDecimal porcentajeIva =  BigDecimal.ZERO;
    private BigDecimal valorIva = BigDecimal.ZERO;
    
    private BigDecimal subtotalNeto;
}
