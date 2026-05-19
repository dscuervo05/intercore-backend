package com.app.pro.service;

import com.app.pro.entity.DetalleVenta;
import com.app.pro.entity.DocumentoVenta;
import com.app.pro.entity.Inventario;
import com.app.pro.repository.DocumentoVentaRepository;
import com.app.pro.repository.InventarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.List;

@Service
public class FacturacionService {
    
    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;
    
    @Autowired
    private InventarioRepository inventarioRepository;
    
    @Transactional
    public DocumentoVenta procesarFactura(DocumentoVenta nuevaFactura) {
        
        // Inicializamos los acumuladores globales en CERO
        BigDecimal subtotalGlobal = BigDecimal.ZERO;
        BigDecimal totalDescuentos = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        // Recorremos cada producto que envió el frontend
        if (nuevaFactura.getDetalles() != null) {
            for (DetalleVenta detalle : nuevaFactura.getDetalles()) {
                
                // 1. CANTIDAD Y PRECIO
                BigDecimal cantidad = new BigDecimal(detalle.getCantidad());
                BigDecimal precioUnitario = detalle.getPrecioUnitario();
                
                // 2. SUBTOTAL BRUTO (Cantidad x Precio)
                BigDecimal subtotalBruto = precioUnitario.multiply(cantidad);
                detalle.setSubtotalBruto(subtotalBruto);

                // 3. DESCUENTOS (Si el frontend no envía descuento, asumimos 0%)
                BigDecimal pDescuento = detalle.getPorcentajeDescuento() != null ? detalle.getPorcentajeDescuento() : BigDecimal.ZERO;
                // Fórmula: (Subtotal * %Descuento) / 100
                BigDecimal valorDescuento = subtotalBruto.multiply(pDescuento)
                                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                detalle.setValorDescuento(valorDescuento);

                // 4. BASE IMPONIBLE (Subtotal Bruto - Descuento)
                BigDecimal baseImponible = subtotalBruto.subtract(valorDescuento);
                detalle.setBaseImponible(baseImponible);

                // 5. CÁLCULO DE IVA (Si el frontend no envía IVA, asumimos 0%)
                BigDecimal pIva = detalle.getPorcentajeIva() != null ? detalle.getPorcentajeIva() : BigDecimal.ZERO;
                // Fórmula: (Base Imponible * %IVA) / 100
                BigDecimal valorIva = baseImponible.multiply(pIva)
                                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                detalle.setValorIva(valorIva);

                // 6. SUBTOTAL NETO DEL RENGLÓN (Base Imponible + IVA)
                BigDecimal subtotalNeto = baseImponible.add(valorIva);
                detalle.setSubtotalNeto(subtotalNeto);

                // Acoplamos la relación bidireccional
                detalle.setDocumentoVenta(nuevaFactura);

                // 7. SUMAMOS A LOS ACUMULADORES GLOBALES DE LA FACTURA
                subtotalGlobal = subtotalGlobal.add(subtotalBruto);
                totalDescuentos = totalDescuentos.add(valorDescuento);
                totalIva = totalIva.add(valorIva);
            }
        }

        // 8. ASIGNAMOS LOS TOTALES GLOBALES A LA FACTURA MAESTRA
        nuevaFactura.setSubtotalGobal(subtotalGlobal);
        nuevaFactura.setTotalDescuento(totalDescuentos);
        
        // Total Base Imponible = Subtotal Global - Descuentos
        BigDecimal totalBaseImponible = subtotalGlobal.subtract(totalDescuentos);
        nuevaFactura.setTotalBaseImponile(totalBaseImponible);
        
        nuevaFactura.setTotalIva(totalIva);
        
        // Total Final = Base Imponible + Total IVA
        nuevaFactura.setTotalFinal(totalBaseImponible.add(totalIva));

        // Finalmente guardamos en PostgreSQL
        return documentoVentaRepository.save(nuevaFactura);
    }
    
    public List<DocumentoVenta> obtenerTodasLasVentas() {
        return documentoVentaRepository.findAll();
    }
}
