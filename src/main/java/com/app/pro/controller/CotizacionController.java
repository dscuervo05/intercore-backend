package com.app.pro.controller;

import com.app.pro.entity.Cotizacion;
import com.app.pro.entity.DocumentoVenta;
import com.app.pro.service.CotizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cotizacion")
@CrossOrigin("*")
public class CotizacionController {
    
    @Autowired
    private CotizacionService cotizacionService;
    
    @PostMapping
    public ResponseEntity<Cotizacion> crearCotizacion(@RequestBody Cotizacion cotizacion){
        Cotizacion nueva = cotizacionService.guardarCotizacion(cotizacion);
        return new ResponseEntity<>(nueva,HttpStatus.CREATED);
    }
    
    @GetMapping
    public List<Cotizacion> listarTodas(){
        return cotizacionService.obtenerTodas();
    }
    
    @PostMapping("/{id}/facurar")
    public ResponseEntity<?> facturarCotizacion(@PathVariable UUID id, @RequestParam String consecutivoFactura){
        try{
            DocumentoVenta facturaGenerada = cotizacionService.convertirAFactura(id, consecutivoFactura);
            return new ResponseEntity<>(facturaGenerada, HttpStatus.CREATED);
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
