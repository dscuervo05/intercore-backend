package com.app.pro.controller;

import com.app.pro.entity.Inventario;
import com.app.pro.service.InventarioService;
import com.app.pro.security.JwtUtil; // Importamos el utilitario
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin("*")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    // 🎯 INYECTAMOS EL EXTRACTOR DE TOKENS
    @Autowired
    private JwtUtil jwtUtil; 

    @GetMapping("/alertas")
    public List<Inventario> obtenerAlertas(@RequestHeader("Authorization") String tokenHeader) {
        
        // Extraemos la empresa de forma segura desde el JWT
        String token = tokenHeader.substring(7);
        String idEmpresaDinamica = jwtUtil.extractIdEmpresa(token);

        // 🎯 Le pasamos la empresa al servicio (¡Esto soluciona el error de compilación!)
        return inventarioService.obtenerAlertasStockBajo(idEmpresaDinamica);
    }
}