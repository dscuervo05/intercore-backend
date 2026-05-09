package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "inventario")
@Data

public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_inventario;
    
    @ManyToOne
    @JoinColumn(name =  "id:_producto")
    private Producto producto;
    
    private String ubicacion;
    private Integer cantidad;
    
    @Column(name = "stock_minimo")
    private Integer stockMinimo;
    
    
}
