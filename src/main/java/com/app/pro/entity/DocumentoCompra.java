package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "documentos_compra")
@Data
public class DocumentoCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_compra;

    @ManyToOne
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @Column(name = "numero_factura", nullable = false)
    private String numeroFactura; // El número de factura física que te entrega el proveedor

    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra = LocalDateTime.now();

    private BigDecimal totalGasto;

    @OneToMany(mappedBy = "documentoCompra", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<DetalleCompra> detalles;
}