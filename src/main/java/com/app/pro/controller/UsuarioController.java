package com.app.pro.controller;

import com.app.pro.entity.Usuario;
import com.app.pro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. Método para VER todos los usuarios
    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    // 2. Método para GUARDAR un nuevo usuario
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario nuevoUsuario) {
        return usuarioRepository.save(nuevoUsuario);
    }
}