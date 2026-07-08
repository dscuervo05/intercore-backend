package com.app.pro.service;

import com.app.pro.dto.LoginRequestDTO;
import com.app.pro.dto.RegisterRequestDTO;
import com.app.pro.entity.Usuario;
import com.app.pro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public String registrarUsuario(RegisterRequestDTO dto){
        
        if (usuarioRepository.findByCorreo(dto.getCorreo()).isPresent()){
            throw new IllegalArgumentException("El correo ya está registrado en la plataforma.");
        }
        
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setCorreo(dto.getCorreo());
        nuevoUsuario.setNombre(dto.getNombre());
        nuevoUsuario.setNombreUsuario(dto.getNombreUsuario());
        nuevoUsuario.setIdEmpresaTenant(dto.getIdEmpresaTenant());
        nuevoUsuario.setRol(dto.getRol().toUpperCase());
        
        String passwordEncriptado = passwordEncoder.encode(dto.getPassword());
        nuevoUsuario.setContrasenaHash(passwordEncriptado);
        
        usuarioRepository.save(nuevoUsuario);
        return "Usuario registrado de manera exitosa";
    }
    
    public String iniciarSesion(LoginRequestDTO dto){
        Usuario usuario = usuarioRepository.findByCorreo(dto.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas o el usuario no existe."));
        
        if(!passwordEncoder.matches(dto.getPassword(), usuario.getContrasenaHash())){
            throw new IllegalArgumentException("Contraseña incorrecta");
        }
        return "Autenticacion exitosa. ¡Bienvenido!";
    }
}
