package com.app.pro.controller;

import com.app.pro.entity.ConfiguracionDian;
import com.app.pro.service.ConfiguracionDianService;
import com.app.pro.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion-dian")
@CrossOrigin("*")
public class ConfiguracionDianController {

    @Autowired
    private ConfiguracionDianService configuracionService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> obtenerConfiguracionDian(@RequestHeader("Authorization") String tokenHeader) {
        try {
            String token = tokenHeader.substring(7);
            String idEmpresaDinamica = jwtUtil.extractIdEmpresa(token);

            ConfiguracionDian config = configuracionService.obtenerConfiguracion(idEmpresaDinamica);
            return ResponseEntity.ok(config);

        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<ConfiguracionDian>  guardarConfiguracion(
            @RequestBody ConfiguracionDian configuracion,
            @RequestHeader("Authorization")  String tokenHeader){

        String token = tokenHeader.substring(7);
        String idEmpresaDinamica = jwtUtil.extractIdEmpresa(token);

        ConfiguracionDian guardada = configuracionService.guardaOActualizarConfiguracion(configuracion,idEmpresaDinamica);
        return new ResponseEntity<>(guardada, HttpStatus.CREATED);
    }

}
