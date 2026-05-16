package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "detalle_cotizacion")
@Data
public class DetalleCotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_detalle_cotizacion;

    @ManyToOne
    @JoinColumn(name = "id_cotizacion", nullable = false)
    @JsonBackReference
    private Cotizacion cotizacion;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}