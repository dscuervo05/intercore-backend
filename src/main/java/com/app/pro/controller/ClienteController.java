package com.app.pro.controller;

import com.app.pro.entity.Cliente;
import com.app.pro.service.ClienteService;
import com.app.pro.security.JwtUtil; // 🎯 Ruta corregida
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin("*")
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;

    // 🎯 Faltaba inyectar el JwtUtil para poder usarlo en los métodos
    @Autowired
    private JwtUtil jwtUtil; 
    
    @GetMapping
    public List<Cliente> listarClientes(@RequestHeader("Authorization") String tokenHeader){
        // Extraemos los datos reales del Token
        String token = tokenHeader.substring(7);
        String idEmpresaDinamica = jwtUtil.extractIdEmpresa(token);
        
        return clienteService.listarTodos(idEmpresaDinamica);
    }
    
    @PostMapping
    public ResponseEntity<?> guardarCliente(
            @RequestBody Cliente cliente,
            @RequestHeader("Authorization") String tokenHeader) {
        try {
            // Extraemos los datos reales del Token
            String token = tokenHeader.substring(7);
            String idEmpresaDinamica = jwtUtil.extractIdEmpresa(token);
            
            Cliente clienteGuardado = clienteService.guardarCliente(cliente, idEmpresaDinamica);
            return new ResponseEntity<>(clienteGuardado, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}