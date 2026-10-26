package com.app.pro.service;

import com.app.pro.entity.*;
import com.app.pro.repository.CotizacionRepository;
import com.app.pro.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CotizacionService {

    @Autowired
    private CotizacionRepository cotizacionRepository;

    @Autowired
    private VentaService ventaService;

    // Necesitamos el repositorio de productos para buscar el precio real
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private InventarioService inventarioService;

    @Transactional
    public Cotizacion guardarCotizacion(Cotizacion nuevaCotizacion, String idEmpresa) {
        
        // 🔒 INYECCIÓN DE SEGURIDAD SAAS
        nuevaCotizacion.setIdEmpresaTenant(idEmpresa);
        
        // Forzamos valores por defecto si no vienen
        if (nuevaCotizacion.getFechaEmision() == null) {
            nuevaCotizacion.setFechaEmision(LocalDateTime.now());
        }
        if (nuevaCotizacion.getEstado() == null) {
            nuevaCotizacion.setEstado("BORRADOR");
        }

        BigDecimal totalCalculado = BigDecimal.ZERO;

        if (nuevaCotizacion.getDetalles() != null) {
            for (DetalleCotizacion detalle : nuevaCotizacion.getDetalles()) {
                
                // 🛡️ REGLA DE ORO FASE 3: El backend busca el precio REAL
                // Asumimos que el frontend nos envía al menos el ID del producto
                Producto productoReal = productoRepository.findById(detalle.getProducto().getIdproducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en BD"));
                
                // Calculamos el subtotal real
                BigDecimal cantidad = new BigDecimal(detalle.getCantidad());
                BigDecimal subtotalReal = productoReal.getPrecio_venta().multiply(cantidad);
                
                // Asignamos los valores verdaderos al detalle
                detalle.setPrecioUnitario(productoReal.getPrecio_venta());
                detalle.setSubtotal(subtotalReal);
                
                // Relación Bidireccional
                detalle.setCotizacion(nuevaCotizacion);
                
                // Sumamos al total de la cotización
                totalCalculado = totalCalculado.add(subtotalReal);
            }
        }
        
        // Guardamos el total calculado por el backend
        nuevaCotizacion.setTotalFinal(totalCalculado);

        return cotizacionRepository.save(nuevaCotizacion);
    }

    // 🎯 Filtro SaaS: Solo devuelve cotizaciones de LA EMPRESA actual
    public List<Cotizacion> obtenerTodas(String idEmpresa) {
        return cotizacionRepository.findByIdEmpresaTenant(idEmpresa);
    }

    @Transactional
    public DocumentoVenta convertirAFactura(UUID idCotizacion, String nuevoConsecutivoFactura, String idEmpresa) {
        
        Cotizacion cotizacion = cotizacionRepository.findById(idCotizacion)
                .orElseThrow(() -> new IllegalArgumentException("La cotización no existe en el sistema."));

        // Validamos que la cotización pertenezca a la empresa que la está solicitando
        if (!cotizacion.getIdEmpresaTenant().equals(idEmpresa)) {
             throw new IllegalArgumentException("No tiene permisos para facturar esta cotización.");
        }

        if ("FACTURADA".equalsIgnoreCase(cotizacion.getEstado())) {
            throw new IllegalArgumentException("Esta cotización ya fue procesada y convertida en factura previamente.");
        }

        DocumentoVenta nuevaVenta = new DocumentoVenta();
        // 🔒 INYECCIÓN DE SEGURIDAD SAAS
        nuevaVenta.setIdEmpresaTenant(idEmpresa); 
        
        nuevaVenta.setCliente(cotizacion.getCliente());
        nuevaVenta.setTipoDocumento("Factura POS"); 
        nuevaVenta.setConsecutivo(nuevoConsecutivoFactura);
        
        // --- ACTUALIZACIÓN FASE 3 ---
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
            
            // --- ACTUALIZACIÓN FASE 3 ---
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
        for (DetalleCotizacion detCoti : cotizacion.getDetalles()) {
            DetalleVenta detVenta = new DetalleVenta();
            detVenta.setProducto(detCoti.getProducto());
            detVenta.setCantidad(detCoti.getCantidad());
            detVenta.setPrecioUnitario(detCoti.getPrecioUnitario());
            
            // ... (tu código de subtotales) ...
            
            detVenta.setDocumentoVenta(nuevaVenta); 
            detallesVenta.add(detVenta);

            // 🎯 EL TOQUE MAESTRO FASE 4: Descontar stock en tiempo real
            inventarioService.descontarStock(detCoti.getProducto().getIdproducto(), detCoti.getCantidad(), idEmpresa);
        }
        
        nuevaVenta.setDetalles(detallesVenta);

        cotizacion.setEstado("FACTURADA");
        cotizacionRepository.save(cotizacion);

        return ventaService.procesarVentaIntegrada(nuevaVenta);
    }
}