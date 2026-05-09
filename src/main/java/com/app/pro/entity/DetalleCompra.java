package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "detalle_compra")
@Data
public class DetalleCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_detalle;

    @ManyToOne
    @JoinColumn(name = "id_compra", nullable = false)
    @JsonBackReference
    private DocumentoCompra documentoCompra;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    private Integer cantidad;
    private BigDecimal costoUnitario; // A cómo te vendieron el producto
    private BigDecimal subtotal;
}