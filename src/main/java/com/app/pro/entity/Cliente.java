package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Data

public class Cliente {
    
    @Column(name = "id_empresa_tenant", nullable = false)
    private String idEmpresaTenant;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_cliente")
    private UUID idCliente;
    
    @Column(name = "tipo_documento" , nullable = false)
    private String tipoDocumento;
    
    @Column(name = "numero_documento", unique = true , nullable = false)
    private String numeroDocumento;
    
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;
    
    private String correo;
    private String telefono;
    private String direccion;
    
    @Column(name = "cupo_credito")
    private BigDecimal cupoCredito = BigDecimal.ZERO;
    
    @Column(name = "creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now();
}
