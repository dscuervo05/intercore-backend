package com.app.pro.controller;

import com.app.pro.entity.Usuario;
import com.app.pro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuarioController {



    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Método para VER todos los usuarios
    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    // 2. Método para GUARDAR un nuevo usuario con BCrypt y UUID
    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario nuevoUsuario) {
        try {
            // Asignar UUID si la entidad no lo generó automáticamente
            if (nuevoUsuario.getId_usuario() == null) {
                nuevoUsuario.setId_usuario(UUID.randomUUID());
            }

            // Cifrar la contraseña enviada en el campo contrasenaHash con BCrypt
            if (nuevoUsuario.getContrasenaHash() != null && !nuevoUsuario.getContrasenaHash().isEmpty()) {
                nuevoUsuario.setContrasenaHash(passwordEncoder.encode(nuevoUsuario.getContrasenaHash()));
            } else {
                return ResponseEntity.badRequest().body("La contraseña es requerida.");
            }

            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar el usuario en BD: " + e.getMessage());
        }
    }
}