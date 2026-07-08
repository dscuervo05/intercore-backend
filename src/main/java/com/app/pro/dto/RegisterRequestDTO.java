package com.app.pro.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
  
    private String correo;
    private String password;
    private String nombre;
    private String nombreUsuario;
    private String idEmpresaTenant;
    private String rol;
    
}
