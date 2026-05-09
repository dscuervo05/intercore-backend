package com.app.pro.controller;

import com.app.pro.entity.DocumentoCompra;
import com.app.pro.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
@CrossOrigin("*")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping
    public ResponseEntity<DocumentoCompra> registrarCompra(@RequestBody DocumentoCompra compra) {
        DocumentoCompra compraGuardada = compraService.procesarCompra(compra);
        return new ResponseEntity<>(compraGuardada, HttpStatus.CREATED);
    }
}