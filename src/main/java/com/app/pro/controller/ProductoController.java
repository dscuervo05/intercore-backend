package com.app.pro.controller;

import com.app.pro.entity.Producto;
import com.app.pro.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin("*")

public class ProductoController {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @GetMapping
    public List<Producto> listar(){
        return productoRepository.findAll();
    }
    
    @PostMapping
    public Producto guardar(@RequestBody Producto producto){
        return productoRepository.save(producto);
    }
    
}
