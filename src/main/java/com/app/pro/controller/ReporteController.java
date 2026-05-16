package com.app.pro.controller;

import com.app.pro.dto.DashboardDTO;
import com.app.pro.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/reportes")
@CrossOrigin("*")
public class ReporteController {
    
    @Autowired
    private ReporteService reporteService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> obtenerDashboard(){
        DashboardDTO datos = reporteService.obtenerDatosDashboard();
        return ResponseEntity.ok(datos);
    }
    
}
