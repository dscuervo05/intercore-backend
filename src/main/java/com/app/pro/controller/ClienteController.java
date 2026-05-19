package com.app.pro.controller;

import com.app.pro.entity.Cliente;
import com.app.pro.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin("*")
public class ClienteController {
    
    // 🎯 Definimos la constante de la empresa para nuestras pruebas de SaaS
    private static final String ID_EMPRESA_TEST = "TORNIMAX_001";
    
    @Autowired
    private ClienteService clienteService;
    
    @GetMapping
    public List<Cliente> listarClientes(){
        // Llamamos al servicio pasando la empresa
        return clienteService.listarTodos(ID_EMPRESA_TEST);
    }
    
    @PostMapping
    public ResponseEntity<?> guardarCliente(@RequestBody Cliente cliente) {
        try {
            // Llamamos al servicio pasando el objeto y la empresa
            Cliente clienteGuardado = clienteService.guardarCliente(cliente, ID_EMPRESA_TEST);
            return new ResponseEntity<>(clienteGuardado, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}