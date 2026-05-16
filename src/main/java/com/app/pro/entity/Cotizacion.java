package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name="cotizaciones")
@Data
public class Cotizacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_cotizacion;
    
    @ManyToOne
    @JoinColumn(name =  "id_Cliente",nullable = false)
    private Cliente cliente;
    
    @Column(unique = true, nullable= false)
    private String consecutivo; //eEmjemplo: COT-0001
    
    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmisio = LocalDateTime.now();
    
    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento = LocalDateTime.now().plusDays(15);
    
    private BigDecimal totalFinal;
    
    @Column(nullable= false)
    private String estado = "PENDIENTE";
    
    @OneToMany(mappedBy =  "cotizacion", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<DetalleCotizacion> detalles;
    
}
