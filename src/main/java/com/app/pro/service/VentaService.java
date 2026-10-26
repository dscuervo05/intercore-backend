package com.app.pro.service;

import com.app.pro.entity.*;
import com.app.pro.repository.CuentaContableRepository;
import com.app.pro.repository.DocumentoVentaRepository;
import com.app.pro.repository.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ContabilidadService contabilidadService;

    // Inyectamos el repositorio contable para buscar las cuentas reales
    @Autowired
    private CuentaContableRepository cuentaRepository;

    @Transactional
    public DocumentoVenta procesarVentaIntegrada(DocumentoVenta nuevaFactura) {
        BigDecimal subtotalGlobal = BigDecimal.ZERO;
        BigDecimal totalDescuentos = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        // 1. CICLO PRINCIPAL: Inventario y Cálculos Matemáticos
        if (nuevaFactura.getDetalles() != null) {
            for (DetalleVenta detalle : nuevaFactura.getDetalles()) {

                // A. Descontar Inventario de forma segura
                Inventario inventario = inventarioRepository
                        .findByIdProductoAndIdEmpresaTenant(detalle.getProducto().getIdproducto(), nuevaFactura.getIdEmpresaTenant())
                        .orElseThrow(() -> new IllegalArgumentException("El producto no está registrado en el inventario"));

                if (inventario.getCantidad() < detalle.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto: " + detalle.getProducto().getNombre());
                }
                inventario.setCantidad(inventario.getCantidad() - detalle.getCantidad());
                inventarioRepository.save(inventario);

                // B. Cálculos Financieros del Renglón
                BigDecimal cantidad = new BigDecimal(detalle.getCantidad());
                BigDecimal precioUnitario = detalle.getPrecioUnitario();
                BigDecimal subtotalBruto = precioUnitario.multiply(cantidad);
                detalle.setSubtotalBruto(subtotalBruto);

                BigDecimal pDescuento = detalle.getPorcentajeDescuento() != null ? detalle.getPorcentajeDescuento() : BigDecimal.ZERO;
                BigDecimal valorDescuento = subtotalBruto.multiply(pDescuento).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                detalle.setValorDescuento(valorDescuento);

                BigDecimal baseImponible = subtotalBruto.subtract(valorDescuento);
                detalle.setBaseImponible(baseImponible); // Usando el nombre exacto de tu entidad

                BigDecimal pIva = detalle.getPorcentajeIva() != null ? detalle.getPorcentajeIva() : BigDecimal.ZERO;
                BigDecimal valorIva = baseImponible.multiply(pIva).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                detalle.setValorIva(valorIva);

                detalle.setSubtotalNeto(baseImponible.add(valorIva));
                detalle.setDocumentoVenta(nuevaFactura);

                // C. Acumular Totales Globales
                subtotalGlobal = subtotalGlobal.add(subtotalBruto);
                totalDescuentos = totalDescuentos.add(valorDescuento);
                totalIva = totalIva.add(valorIva);
            }
        }

        // 2. CONSOLIDACIÓN DE LA FACTURA
        nuevaFactura.setSubtotalGobal(subtotalGlobal);
        nuevaFactura.setTotalDescuento(totalDescuentos);
        BigDecimal totalBaseImponible = subtotalGlobal.subtract(totalDescuentos);
        nuevaFactura.setTotalBaseImponile(totalBaseImponible); // Usando el nombre exacto de tu entidad
        nuevaFactura.setTotalIva(totalIva);
        nuevaFactura.setTotalFinal(totalBaseImponible.add(totalIva));

        DocumentoVenta ventaGuardada = documentoVentaRepository.save(nuevaFactura);

        // 3. INTEGRACIÓN CONTABLE AUTOMÁTICA
        generarAsientoContable(ventaGuardada);

        return ventaGuardada;
    }

    private void generarAsientoContable(DocumentoVenta venta) {
        String idEmpresa = venta.getIdEmpresaTenant();

        ComprobanteDiario comprobante = new ComprobanteDiario();
        comprobante.setFechaContable(LocalDate.now());
        comprobante.setDescripcion("Venta POS - Factura " + venta.getConsecutivo());

        List<MovimientoContable> movimientos = new ArrayList<>();

        // DÉBITO: Ingreso a Caja/Bancos (Total de la factura)
        CuentaContable cuentaCaja = cuentaRepository.findByCodigoAndIdEmpresaTenant("110505", idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de Caja (110505) no configurada."));

        MovimientoContable debitoCaja = new MovimientoContable();
        debitoCaja.setCuenta(cuentaCaja);
        debitoCaja.setDebito(venta.getTotalFinal());
        debitoCaja.setReferenciaDocumento(venta.getConsecutivo());
        movimientos.add(debitoCaja);

        // CRÉDITO: Ingreso por Ventas (Base imponible)
        CuentaContable cuentaIngresos = cuentaRepository.findByCodigoAndIdEmpresaTenant("4135", idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de Ingresos (4135) no configurada."));

        MovimientoContable creditoVentas = new MovimientoContable();
        creditoVentas.setCuenta(cuentaIngresos);
        creditoVentas.setCredito(venta.getTotalBaseImponile());
        creditoVentas.setReferenciaDocumento(venta.getConsecutivo());
        movimientos.add(creditoVentas);

        // CRÉDITO: IVA Generado (Solo si hay IVA)
        if (venta.getTotalIva().compareTo(BigDecimal.ZERO) > 0) {
            CuentaContable cuentaIva = cuentaRepository.findByCodigoAndIdEmpresaTenant("2408", idEmpresa)
                    .orElseThrow(() -> new IllegalArgumentException("Cuenta de IVA (2408) no configurada."));

            MovimientoContable creditoIva = new MovimientoContable();
            creditoIva.setCuenta(cuentaIva);
            creditoIva.setCredito(venta.getTotalIva());
            creditoIva.setReferenciaDocumento(venta.getConsecutivo());
            movimientos.add(creditoIva);
        }

        // Ejecutamos la validación de partida doble y guardado delegando al ContabilidadService
        contabilidadService.registrarAsiento(comprobante, movimientos, idEmpresa);
    }

    public List<DocumentoVenta> obtenerVentasPorEmpresa(String idEmpresaTenant) {
        return documentoVentaRepository.findByIdEmpresaTenant(idEmpresaTenant);
    }
}