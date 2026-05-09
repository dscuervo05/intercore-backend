package com.app.pro.service;

import com.app.pro.entity.Inventario;
import com.app.pro.repository.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventarioService {
    
    @Autowired
    private InventarioRepository inventarioRepository;
    
    public List<Inventario> obtenerAlertasStockBajo(){
        List<Inventario> todoElInventario = inventarioRepository.findAll();
        return todoElInventario.stream().filter(inv -> inv.getCantidad()<= inv.getStockMinimo()).collect(Collectors.toList());
    } 
}
