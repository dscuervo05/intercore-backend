package com.app.pro.service;

import com.app.pro.entity.*;
import com.app.pro.repository.CotizacionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CotizacionService {
    
    @Autowired
    private CotizacionRepository cotizacionRepository;
    
    @Autowired
    private FacturacionService facturacionService;
    
    public Cotizacion guardarCotizacion(Cotizacion nuevaCotizacion){
        if(nuevaCotizacion.getDetalles() !=null){
            for(DetalleCotizacion detalle : nuevaCotizacion.getDetalles()){
                detalle.setCotizacion(nuevaCotizacion);
            }
        }
        return cotizacionRepository.save(nuevaCotizacion);
    }
    
    public List<Cotizacion> obtenerTodas(){
        return cotizacionRepository.findAll();
    }
    
    @Transactional
    public DocumentoVenta convertirAFactura(UUID idCotizacion, String nuevoConsecutivoFatura){
        Cotizacion cotizacion = cotizacionRepository.findById(idCotizacion)
                .orElseThrow(()-> new IllegalArgumentException("La cotización no existe en el sistema."));
        if("FACTURA".equalsIgnoreCase(cotizacion.getEstado())){
            throw new IllegalArgumentException("Esta cotización ya fue procesada y convertida en factura previamente.");
        }
        
        DocumentoVenta nuevaVenta = new DocumentoVenta();
        nuevaVenta.setCliente(cotizacion.getCliente());
        nuevaVenta.setTipoDocumento("Factura POS");
        nuevaVenta.setConsecutivo(nuevoConsecutivoFatura);
        nuevaVenta.setTotalFinal(cotizacion.getTotalFinal());
        
        List<DetalleVenta> detallesVenta = new ArrayList<>();
        for(DetalleCotizacion detCoti : cotizacion.getDetalles()){
            DetalleVenta detVenta = new DetalleVenta();
            detVenta.setProducto(detCoti.getProducto());
            detVenta.setCantidad(detCoti.getCantidad());
            detVenta.setPrecioUnitario(detCoti.getPrecioUnitario());
            detVenta.setSubtotal(detCoti.getSubtotal());
            detVenta.setDocumentoVenta(nuevaVenta);
            detallesVenta.add(detVenta);
        }
        
        nuevaVenta.setDetalles(detallesVenta);
        
        cotizacion.setEstado("FACTURADA");
        cotizacionRepository.save(cotizacion);
        
        return facturacionService.procesarVenta(nuevaVenta);
    }
}
