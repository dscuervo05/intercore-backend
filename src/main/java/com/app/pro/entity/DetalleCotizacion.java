package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "detalle_cotizacion")
@Data
public class DetalleCotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idDetalle;

    private Integer cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    // El @JsonIgnore evita ciclos infinitos al enviar datos al frontend
    @ManyToOne
    @JoinColumn(name = "id_cotizacion", nullable = false)
    @JsonIgnore 
    private Cotizacion cotizacion;
}