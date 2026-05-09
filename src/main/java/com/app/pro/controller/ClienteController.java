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
    
    @Autowired
    private ClienteService clienteService;
    
    @GetMapping
    public List<Cliente> listarClientes(){
        return clienteService.listarTodos();
    }
    
    @PostMapping
    public ResponseEntity<?> guardarCliente(@RequestBody Cliente cliente) {
        try {
            // Intentamos guardar usando nuestra lógica de negocio
            Cliente clienteGuardado = clienteService.guardarCliente(cliente);
            return new ResponseEntity<>(clienteGuardado, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            // Si el servicio detecta un duplicado, le mandamos el texto del error al frontend de Fernanda
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}

