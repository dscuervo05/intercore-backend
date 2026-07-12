package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "movimientos_contables")
@Data
public class MovimientoContable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private ComprobanteDiario comprobante;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private CuentaContable cuenta;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal debito = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal credito = BigDecimal.ZERO;
    
    @Column(length = 50)
    private String referenciaDocumento;
    
    @Column(name = "id_empresa_tenant", nullable = false)
    private String idEmpresaTenant;
    
}
