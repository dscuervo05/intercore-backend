package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name="cuentas_contables")
@Data
public class CuentaContable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String codigo;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(nullable = false, length = 20)
    private String tipoClase;
    
    @Column(nullable = false, length = 15)
    private String naturaleza;
    
    @Column(name = "id_empresa_tenant", nullable = false)
    private String idEmpresaTenant;
    
}
