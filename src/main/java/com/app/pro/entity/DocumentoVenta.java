package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "documentos_venta")
@Data

public class DocumentoVenta {
    
    @Column(name = "id_empresa_tenant", nullable = false)
    private String idEmpresaTenant;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID  id_documento;
    
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false )
    private Cliente cliente;
    private String tipoDocumento;
    
    @Column(unique = true, nullable = false)
    private String consecutivo;
    
    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision = LocalDateTime.now();
    
    @Column(name = "subtotal_global")
    private BigDecimal subtotalGobal = BigDecimal.ZERO;
    
    @Column(name = "total_descuento")
    private BigDecimal totalDescuento = BigDecimal.ZERO;
    
    @Column(name = "total_base_imponible")
    private BigDecimal totalBaseImponile = BigDecimal.ZERO;
    
    @Column(name = "total_iva")
    private BigDecimal totalIva = BigDecimal.ZERO;
    
    @Column(name = "total_final")
    private BigDecimal totalFinal = BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "documentoVenta", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<DetalleVenta> detalles;
    
    
}
