package com.app.pro.controller;

import com.app.pro.entity.DocumentoCompra;
import com.app.pro.service.CompraService;
import com.app.pro.security.JwtUtil; // 🎯 Importamos la herramienta del token
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
@CrossOrigin("*")
public class CompraController {

    // 🎯 Corregido el typo: "CompraService" sin 's'
    @Autowired
    private CompraService compraService;

    // 🎯 Inyectamos el extractor de tokens
    @Autowired
    private JwtUtil jwtUtil; 

    @PostMapping
    public ResponseEntity<DocumentoCompra> registrarCompra(
            @RequestBody DocumentoCompra compra,
            @RequestHeader("Authorization") String tokenHeader) { // 🔐 Exigimos el token
        
        // 1. Abrimos el token y sacamos la empresa de forma dinámica
        String token = tokenHeader.substring(7);
        String idEmpresaDinamica = jwtUtil.extractIdEmpresa(token);
        
        // 2. Le pasamos AMBOS datos al servicio (¡Adiós línea roja!)
        DocumentoCompra compraGuardada = compraService.procesarCompra(compra, idEmpresaDinamica);
        
        return new ResponseEntity<>(compraGuardada, HttpStatus.CREATED);
    }
}