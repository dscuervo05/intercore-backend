package com.app.pro.controller;

import com.app.pro.entity.Inventario;
import com.app.pro.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin("*")

public class InventarioController {
    
    @Autowired
    private InventarioService inventarioService;
    
    @GetMapping("/alertas")
    public List<Inventario> obtenerAlertas(){
        return inventarioService.obtenerAlertasStockBajo();
    }
    
}
