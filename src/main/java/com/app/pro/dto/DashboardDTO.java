package com.app.pro.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardDTO {
    
    private long totalClientes;
    private long totalProductos;
    private long totalVentas;
    private BigDecimal ingresosTotales;
    
}
