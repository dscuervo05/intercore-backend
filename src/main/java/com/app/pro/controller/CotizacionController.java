package com.app.pro.controller;

import com.app.pro.entity.Cotizacion;
import com.app.pro.entity.DocumentoVenta;
import com.app.pro.service.CotizacionService;
import com.app.pro.security.JwtUtil; // Asegúrate de importar tu JwtUtil
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

    @Autowired
    private JwtUtil jwtUtil; // 🎯 Inyectamos el utilitario del Token
    
    @PostMapping
    public ResponseEntity<Cotizacion> crearCotizacion(
            @RequestBody Cotizacion cotizacion,
            @RequestHeader("Authorization") String tokenHeader) { // 🔍 Interceptamos la cabecera segura
        
        // El token viene como "Bearer eyJhbGci...", le quitamos los primeros 7 caracteres
        String token = tokenHeader.substring(7);
        String idEmpresaDinamica = jwtUtil.extractIdEmpresa(token); // 🔑 ¡Extracción limpia!
        
        Cotizacion nueva = cotizacionService.guardarCotizacion(cotizacion, idEmpresaDinamica);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }
    
    @GetMapping
    public List<Cotizacion> listarTodas(@RequestHeader("Authorization") String tokenHeader){
        String token = tokenHeader.substring(7);
        String idEmpresaDinamica = jwtUtil.extractIdEmpresa(token);
        
        return cotizacionService.obtenerTodas(idEmpresaDinamica);
    }
    
    @PostMapping("/{id}/facturar")
    public ResponseEntity<?> facturarCotizacion(
            @PathVariable UUID id, 
            @RequestParam String consecutivoFactura,
            @RequestHeader("Authorization") String tokenHeader){
        try{
            String token = tokenHeader.substring(7);
            String idEmpresaDinamica = jwtUtil.extractIdEmpresa(token);
            
            DocumentoVenta facturaGenerada = cotizacionService.convertirAFactura(id, consecutivoFactura, idEmpresaDinamica);
            return new ResponseEntity<>(facturaGenerada, HttpStatus.CREATED);
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}