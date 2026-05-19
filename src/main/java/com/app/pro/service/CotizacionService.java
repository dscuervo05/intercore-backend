package com.app.pro.service;

import com.app.pro.entity.*;
import com.app.pro.repository.CotizacionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CotizacionService {

    @Autowired
    private CotizacionRepository cotizacionRepository;

    @Autowired
    private FacturacionService facturacionService; 

    public Cotizacion guardarCotizacion(Cotizacion nuevaCotizacion) {
        if (nuevaCotizacion.getDetalles() != null) {
            for (DetalleCotizacion detalle : nuevaCotizacion.getDetalles()) {
                detalle.setCotizacion(nuevaCotizacion);
            }
        }
        return cotizacionRepository.save(nuevaCotizacion);
    }

    public List<Cotizacion> obtenerTodas() {
        return cotizacionRepository.findAll();
    }

    @Transactional
    public DocumentoVenta convertirAFactura(UUID idCotizacion, String nuevoConsecutivoFactura) {
        
        Cotizacion cotizacion = cotizacionRepository.findById(idCotizacion)
                .orElseThrow(() -> new IllegalArgumentException("La cotización no existe en el sistema."));

        if ("FACTURADA".equalsIgnoreCase(cotizacion.getEstado())) {
            throw new IllegalArgumentException("Esta cotización ya fue procesada y convertida en factura previamente.");
        }

        DocumentoVenta nuevaVenta = new DocumentoVenta();
        nuevaVenta.setCliente(cotizacion.getCliente());
        nuevaVenta.setTipoDocumento("Factura POS"); 
        nuevaVenta.setConsecutivo(nuevoConsecutivoFactura);
        
        // --- ACTUALIZACIÓN FASE 3: Llenamos los nuevos campos globales de impuestos ---
        nuevaVenta.setSubtotalGobal(cotizacion.getTotalFinal());
        nuevaVenta.setTotalBaseImponile(cotizacion.getTotalFinal());
        nuevaVenta.setTotalFinal(cotizacion.getTotalFinal());
        nuevaVenta.setTotalDescuento(BigDecimal.ZERO);
        nuevaVenta.setTotalIva(BigDecimal.ZERO);

        List<DetalleVenta> detallesVenta = new ArrayList<>();
        
        for (DetalleCotizacion detCoti : cotizacion.getDetalles()) {
            DetalleVenta detVenta = new DetalleVenta();
            detVenta.setProducto(detCoti.getProducto());
            detVenta.setCantidad(detCoti.getCantidad());
            detVenta.setPrecioUnitario(detCoti.getPrecioUnitario());
            
            // --- ACTUALIZACIÓN FASE 3: Llenamos la cascada por cada producto ---
            detVenta.setSubtotalBruto(detCoti.getSubtotal());
            detVenta.setBaseImponible(detCoti.getSubtotal());
            detVenta.setSubtotalNeto(detCoti.getSubtotal());
            
            detVenta.setPorcentajeDescuento(BigDecimal.ZERO);
            detVenta.setValorDescuento(BigDecimal.ZERO);
            detVenta.setPorcentajeIva(BigDecimal.ZERO);
            detVenta.setValorIva(BigDecimal.ZERO);
            
            detVenta.setDocumentoVenta(nuevaVenta); 
            detallesVenta.add(detVenta);
        }
        
        nuevaVenta.setDetalles(detallesVenta);

        cotizacion.setEstado("FACTURADA");
        cotizacionRepository.save(cotizacion);

        return facturacionService.procesarFactura(nuevaVenta);
    }
}