package com.app.pro.service;

import com.app.pro.entity.DetalleCompra;
import com.app.pro.entity.DocumentoCompra;
import com.app.pro.entity.Inventario;
import com.app.pro.repository.DocumentoCompraRepository;
import com.app.pro.repository.InventarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CompraService {

    @Autowired
    private DocumentoCompraRepository compraRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    // @Autowired
    // private ContabilidadService contabilidadService; // Descomentar cuando integres la contabilidad automática

    @Transactional
    public DocumentoCompra procesarCompra(DocumentoCompra nuevaCompra, String idEmpresa) {

        // 1. Asignar el Tenant a la compra
        nuevaCompra.setIdEmpresaTenant(idEmpresa);

        List<DetalleCompra> detalles = nuevaCompra.getDetalles();
        BigDecimal totalCalculado = BigDecimal.ZERO;

        if (detalles != null && !detalles.isEmpty()) {
            for (DetalleCompra detalle : detalles) {
                // Enlazar el detalle con su factura
                detalle.setDocumentoCompra(nuevaCompra);

                // Calcular subtotal de este ítem (Asumiendo que DetalleCompra tiene costoUnitario y cantidad)
                // BigDecimal subtotal = detalle.getCostoUnitario().multiply(new BigDecimal(detalle.getCantidad()));
                // totalCalculado = totalCalculado.add(subtotal);

                // 2. Buscar en inventario con método SaaS (UUID del producto y la empresa)
                Optional<Inventario> inventarioOpt = inventarioRepository.findByIdProductoAndIdEmpresaTenant(
                        detalle.getProducto().getIdproducto(),
                        idEmpresa
                );

                if (inventarioOpt.isPresent()) {
                    Inventario inv = inventarioOpt.get();
                    inv.setCantidad(inv.getCantidad() + detalle.getCantidad());
                    inventarioRepository.save(inv);
                } else {
                    Inventario nuevoInventario = new Inventario();
                    nuevoInventario.setProducto(detalle.getProducto());
                    nuevoInventario.setCantidad(detalle.getCantidad());
                    nuevoInventario.setStockMinimo(5);
                    nuevoInventario.setUbicacion("Bodega Principal");
                    nuevoInventario.setIdEmpresaTenant(idEmpresa);

                    inventarioRepository.save(nuevoInventario);
                }
            }
        }

        // 3. Asignar el total calculado a la compra
        nuevaCompra.setTotalGasto(totalCalculado);

        // 4. 🎯 GUARDAR LA COMPRA (Esto faltaba para persistir cabecera y detalles en cascada)
        DocumentoCompra compraGuardada = compraRepository.save(nuevaCompra);

        // 5. Generar Asiento Contable Automático (Opcional según tu avance)
        // contabilidadService.registrarAsientoCompra(compraGuardada, idEmpresa);

        return compraGuardada;
    }
}