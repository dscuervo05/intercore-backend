package com.app.pro.controller;

import com.app.pro.entity.DocumentoVenta;
import com.app.pro.security.JwtUtil;
import com.app.pro.service.DianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/facturacion-electronica")
public class FacturacionElectronicaController {

    @Autowired
    private DianService dianService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/{idVenta}/transmitir")
    public ResponseEntity<?> transmitirFactura(
            @PathVariable UUID idVenta,
            @RequestHeader("Authorization") String tokenHeader) {

        try {
            String token = tokenHeader.substring(7);
            String idEmpresaTenant = jwtUtil.extractIdEmpresa(token);

            DocumentoVenta ventaProcesada = dianService.procesarYTransmitirFactura(idVenta, idEmpresaTenant);
            return ResponseEntity.ok(ventaProcesada);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error durante la emisión: " + e.getMessage());
        }
    }

    @GetMapping("/{idVenta}/estado/{trackId}")
    public ResponseEntity<?> consultarEstadoDian(
            @PathVariable UUID idVenta,
            @PathVariable String trackId,
            @RequestHeader("Authorization") String tokenHeader) {

        try {
            String token = tokenHeader.substring(7);
            String idEmpresaTenant = jwtUtil.extractIdEmpresa(token);

            DocumentoVenta ventaActualizada = dianService.consultarEstadoDian(idVenta, trackId, idEmpresaTenant);
            return ResponseEntity.ok(ventaActualizada);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al consultar estado DIAN: " + e.getMessage());
        }
    }
}