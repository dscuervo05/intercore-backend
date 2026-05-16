package com.app.pro.service;

import com.app.pro.dto.DashboardDTO;
import com.app.pro.repository.ClienteRepository;
import com.app.pro.repository.ProductoRepository;
import com.app.pro.repository.DocumentoVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ReporteService {
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private DocumentoVentaRepository ventaRepository;
    
    public DashboardDTO obtenerDatosDashboard(){
        
        DashboardDTO resumen = new DashboardDTO();
 
        // 1. Contamos la cantidad de registros en cada tabla usando el método count() de Spring
        resumen.setTotalClientes(clienteRepository.count());
        resumen.setTotalProductos(productoRepository.count());
        resumen.setTotalVentas(ventaRepository.count());
        
        // 2. Sumamos los ingresos (y si no hay ventas aún, evitamos un error enviando CERO)
        BigDecimal ingresos = ventaRepository.sumarIngresosTotales();
        resumen.setIngresosTotales( ingresos != null ? ingresos : BigDecimal.ZERO);
        
        return resumen;
    }
    
}
