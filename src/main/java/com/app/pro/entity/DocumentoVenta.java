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
    
    private BigDecimal totalFinal;
    
    @OneToMany(mappedBy = "documentoVenta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles;
    
    
}
