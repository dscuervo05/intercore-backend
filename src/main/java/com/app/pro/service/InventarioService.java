package com.app.pro.service;

import com.app.pro.entity.Inventario;
import com.app.pro.repository.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventarioService {
    
    @Autowired
    private InventarioRepository inventarioRepository;
    
    // 🎯 MODIFICADO: Exigimos la empresa para no mezclar inventarios
    public List<Inventario> obtenerAlertasStockBajo(String idEmpresa){
        // 1. Buscamos SOLAMENTE el inventario de la empresa que lo solicita
        List<Inventario> inventarioEmpresa = inventarioRepository.findByIdEmpresaTenant(idEmpresa);
        
        // 2. Filtramos con tu lógica de Streams (¡que está perfecta!)
        return inventarioEmpresa.stream()
                .filter(inv -> inv.getCantidad() <= inv.getStockMinimo())
                .collect(Collectors.toList());
    }
    
@Transactional
public void descontarStock(UUID idProducto, int cantidadVendida, String idEmpresa) {
    
    // 1. Buscamos cuánto hay en la bodega real de esa empresa
    Inventario stockActual = inventarioRepository.findByIdProductoAndIdEmpresaTenant(idProducto, idEmpresa)
        .orElseThrow(() -> new IllegalArgumentException("El producto no está inicializado en el inventario."));

    // 2. Escudo protector
    if (stockActual.getCantidad() < cantidadVendida) {
        throw new IllegalArgumentException("Stock insuficiente. Solo quedan " + stockActual.getCantidad() + " unidades disponibles.");
    }

    // 3. Matemática de sustracción
    stockActual.setCantidad(stockActual.getCantidad() - cantidadVendida);
    inventarioRepository.save(stockActual);
}}
