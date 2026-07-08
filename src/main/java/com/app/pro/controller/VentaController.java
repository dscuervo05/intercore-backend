package com.app.pro.controller;

import com.app.pro.entity.Usuario;
import com.app.pro.entity.DocumentoVenta;
import com.app.pro.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin("*")
public class VentaController {
    
    @Autowired
    private VentaService ventaService;
    
    @PostMapping("/crear")
    @PreAuthorize("hasAnyRole ('ADMIN','MOSTRADOR')")
    public ResponseEntity<?> resgistrarVenta(@RequestBody DocumentoVenta nuevaVenta){
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogueado = (Usuario) authentication.getPrincipal();
        
        nuevaVenta.setIdEmpresaTenant(usuarioLogueado.getIdEmpresaTenant());
        
        DocumentoVenta ventaGuardada = ventaService.guardarVenta(nuevaVenta);
        return ResponseEntity.ok(ventaGuardada);
    }
    
    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
    public ResponseEntity<List<DocumentoVenta>> obtenerHistorialVentas(){
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogueado = (Usuario) authentication.getPrincipal();
        
        String idEmpresaTenant = usuarioLogueado.getIdEmpresaTenant();
        List<DocumentoVenta> ventasDelaEmpresa = ventaService.obtenerVentasPorEmpresa(idEmpresaTenant);
        
        return ResponseEntity.ok(ventasDelaEmpresa);
        
    }
    
}
