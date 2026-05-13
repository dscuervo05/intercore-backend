package com.app.pro.dto;

public class AuthRequest {
    
    private String correo;
    private String contrasena;
    
    public String getCorreo(){
        return correo;
    }
    
    public void getCorreo(String correo){
        this.correo = correo;
    }
    
    public String getContrasena(){
        return contrasena;
    }
    
    public void setContrasena(String contrasena){           
        this.contrasena = contrasena;
    }

}
