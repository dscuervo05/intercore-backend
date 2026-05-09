package com.app.pro.service;

import com.app.pro.entity.DetalleVenta;
import com.app.pro.entity.DocumentoVenta;
import com.app.pro.entity.Inventario;
import com.app.pro.repository.DocumentoVentaRepository;
import com.app.pro.repository.InventarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacturacionService {
    
    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;
    
    @Autowired
    private InventarioRepository inventarioRepository;
    
    @Transactional
    public DocumentoVenta procesarVenta(DocumentoVenta nuevaVenta) {
        
        List<DetalleVenta> detalles = nuevaVenta.getDetalles();
        
        if (detalles != null && !detalles.isEmpty()) {
            for (DetalleVenta detalle : detalles) {
                // 1. Asignar la cabecera al detalle
                detalle.setDocumentoVenta(nuevaVenta);

                // 2. Buscar el inventario de este producto específico
                Inventario inventario = inventarioRepository.findFirstByProducto(detalle.getProducto())
                        .orElseThrow(() -> new IllegalArgumentException("No hay registro de inventario para el producto proporcionado."));

                // 3. Validar que tengamos suficiente mercancía
                if (inventario.getCantidad() < detalle.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente. Solo quedan " + inventario.getCantidad() + " unidades disponibles.");
                }

                // 4. Restar la cantidad vendida del inventario
                inventario.setCantidad(inventario.getCantidad() - detalle.getCantidad());
                inventarioRepository.save(inventario);
            }
        }
        
        return documentoVentaRepository.save(nuevaVenta);
    }
    
    public List<DocumentoVenta> obtenerTodasLasVentas() {
        return documentoVentaRepository.findAll();
    }
}
