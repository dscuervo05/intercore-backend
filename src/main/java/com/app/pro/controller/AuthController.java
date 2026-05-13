package com.app.pro.controller;

import com.app.pro.dto.AuthRequest;
import com.app.pro.security.CustomUserDetailsService;
import com.app.pro.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> crearTokenAutenticacion(@RequestBody AuthRequest authRequest) {
        try {
            // 1. Intentar autenticar con el correo y contraseña reales
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getCorreo(), authRequest.getContrasena())
            );
        } catch (BadCredentialsException e) {
            // Si la contraseña es incorrecta, devolvemos un error 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Correo o contraseña incorrectos");
        }

        // 2. Si pasó, buscamos los datos del usuario
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getCorreo());

        // 3. Generamos el Token JWT
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. Lo devolvemos en formato JSON
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        return ResponseEntity.ok(response);
    }
    
    // ... tus otras inyecciones (AuthenticationManager, JwtUtil, etc) ...

    @Autowired
    private com.app.pro.repository.UsuarioRepository usuarioRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // NUEVO ENDPOINT PARA REGISTRAR USUARIOS LIMPIOS
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody AuthRequest request) {
        
        // Verificamos que el correo no exista ya
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            return ResponseEntity.badRequest().body("El correo ya está registrado");
        }

        com.app.pro.entity.Usuario nuevoUsuario = new com.app.pro.entity.Usuario();
        nuevoUsuario.setCorreo(request.getCorreo());
        
        // Aquí ocurre la magia: Spring Boot encripta la contraseña de forma segura y sin espacios
        nuevoUsuario.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        
        // Llenamos los campos obligatorios por defecto
        nuevoUsuario.setNombre("Usuario Nuevo");
        nuevoUsuario.setNombreUsuario(request.getCorreo()); // Usamos el correo como username temporal
        nuevoUsuario.setRol("ADMIN");

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok("Usuario registrado correctamente en Cuervo Lab");
    }
}