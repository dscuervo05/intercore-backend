package com.app.pro.controller;

import com.app.pro.entity.DocumentoVenta;
import com.app.pro.service.FacturacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin("*")
public class FacturacionController {
    
    @Autowired
    private FacturacionService facturacionService;
    
    @PostMapping
    public ResponseEntity<DocumentoVenta> crearVenta(@RequestBody DocumentoVenta venta) {
        DocumentoVenta ventaGuardada = facturacionService.procesarVenta(venta);
        return new ResponseEntity<>(ventaGuardada, HttpStatus.CREATED);
    }
    
    @GetMapping
    public List<DocumentoVenta> listarVentas(){
        return facturacionService.obtenerTodasLasVentas();
    }
    
}
