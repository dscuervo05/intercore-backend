package com.app.pro.service;

import com.app.pro.entity.DocumentoVenta;
import com.app.pro.entity.Inventario;
import com.app.pro.repository.DocumentoVentaRepository;
import com.app.pro.repository.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;

@Service
public class VentaService {
    
    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;
    
    @Autowired
    private InventarioRepository inventarioRepository;
    
    @Transactional
    public DocumentoVenta guardarVenta (DocumentoVenta venta){
        
        if(venta.getDetalles() != null){
            for(var detalle : venta.getDetalles()){
                Inventario inventario = inventarioRepository
                        .findByIdProductoAndIdEmpresaTenant(detalle.getProducto().getIdproducto(), venta.getIdEmpresaTenant())
                        .orElseThrow(() -> new IllegalArgumentException("El producto no esta registrado en el inventario"));
                if(inventario.getCantidad() < detalle.getCantidad()){
                    throw new IllegalArgumentException("Stock insuficiente para el producto: " + detalle.getProducto().getNombre());
                }
                inventario.setCantidad(inventario.getCantidad() - detalle.getCantidad());
                inventarioRepository.save(inventario);
            }
        }
        return documentoVentaRepository.save(venta);
    }
    
    public List<DocumentoVenta> obtenerVentasPorEmpresa(String idEmpresaTenant){
        return documentoVentaRepository.findByIdEmpresaTenant(idEmpresaTenant);
    }
    
}
