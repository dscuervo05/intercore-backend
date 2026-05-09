package com.app.pro.service;

import com.app.pro.entity.DetalleCompra;
import com.app.pro.entity.DocumentoCompra;
import com.app.pro.entity.Inventario;
import com.app.pro.repository.DocumentoCompraRepository;
import com.app.pro.repository.InventarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompraService {

    @Autowired
    private DocumentoCompraRepository compraRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Transactional
    public DocumentoCompra procesarCompra(DocumentoCompra nuevaCompra) {
        
        List<DetalleCompra> detalles = nuevaCompra.getDetalles();
        
        if (detalles != null && !detalles.isEmpty()) {
            for (DetalleCompra detalle : detalles) {
                // 1. Enlazar el detalle con su factura
                detalle.setDocumentoCompra(nuevaCompra);

                // 2. Buscar si el producto ya existe en el inventario
                Optional<Inventario> inventarioOpt = inventarioRepository.findFirstByProducto(detalle.getProducto());
                
                if (inventarioOpt.isPresent()) {
                    // Si existe, le SUMAMOS la cantidad comprada
                    Inventario inv = inventarioOpt.get();
                    inv.setCantidad(inv.getCantidad() + detalle.getCantidad());
                    inventarioRepository.save(inv);
                } else {
                    // Si es nuevo, inicializamos su stock por primera vez
                    Inventario nuevoInventario = new Inventario();
                    nuevoInventario.setProducto(detalle.getProducto());
                    nuevoInventario.setCantidad(detalle.getCantidad());
                    nuevoInventario.setStockMinimo(5); // Una alerta por defecto
                    nuevoInventario.setUbicacion("Bodega Principal");
                    inventarioRepository.save(nuevoInventario);
                }
            }
        }
        
        return compraRepository.save(nuevaCompra);
    }
}