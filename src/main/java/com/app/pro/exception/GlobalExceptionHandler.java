package com.app.pro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarReglasDeNegocio(IllegalArgumentException ex){
        Map<String, Object> respuesta = new HashMap<>();
        
        respuesta.put("fechaHora", LocalDateTime.now());
        respuesta.put("estado", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", "Error de Validacion");
        respuesta.put("mensaje", ex.getMessage());
        
        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> manejarErroresGenerales(Exception ex){
        Map<String,Object> respuesta = new HashMap<>();
        
        respuesta.put("fechaHora", LocalDateTime.now());
        respuesta.put("estado", HttpStatus.INTERNAL_SERVER_ERROR.value());
        respuesta.put("error", "Error interno del Servidor");
        respuesta.put("mensaje", "Ocurrio un errorinesperado en elsistema.");
        System.out.println("ERROR CRITICO" + ex.getMessage());
        
        return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR );
    }
    
}
