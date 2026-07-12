package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes_diarios")
@Data
public class ComprobanteDiario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate fechaContable;
    
    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    
    @Column(nullable = false, length = 250)
    private String descripcion;
    
    @Column(name= "id_empresa_tenant", nullable = false)
    private String idEmpresaTenant;
    
}
