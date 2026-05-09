package com.app.pro.controller;

import com.app.pro.entity.Proveedor;
import com.app.pro.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin("*")
public class ProveedorController {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @GetMapping
    public List<Proveedor> listar() {
        return proveedorRepository.findAll();
    }

    @PostMapping
    public Proveedor guardar(@RequestBody Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }
}