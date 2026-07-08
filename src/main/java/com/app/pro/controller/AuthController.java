package com.app.pro.controller;

import com.app.pro.dto.AuthRequest;
import com.app.pro.entity.Usuario;
import com.app.pro.repository.UsuarioRepository;
import com.app.pro.security.CustomUserDetailsService;
import com.app.pro.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🎯 EL ÚNICO ENDPOINT DE LOGIN (Fusión de tu código y la lógica SaaS)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // 1. Intentar autenticar con el correo y contraseña reales
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getCorreo(), authRequest.getContrasena())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Correo o contraseña incorrectos");
        }

        // 2. Si pasó, buscamos los datos para Spring Security
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getCorreo());

        // 3. Buscamos el usuario real en BD para saber a qué empresa pertenece
        Usuario usuarioReal = usuarioRepository.findByCorreo(authRequest.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));

        // 4. 🎯 EL TOQUE MAESTRO SAAS: Generamos el Token inyectando LA EMPRESA
        final String jwt = jwtUtil.generateToken(userDetails, usuarioReal.getIdEmpresaTenant());

        // 5. Lo devolvemos en formato JSON al frontend
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        return ResponseEntity.ok(response);
    }
    
    // ENDPOINT PARA REGISTRAR USUARIOS
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody AuthRequest request) {
        
        // Verificamos que el correo no exista ya
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            return ResponseEntity.badRequest().body("El correo ya está registrado");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setCorreo(request.getCorreo());
        nuevoUsuario.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        nuevoUsuario.setNombre("Usuario Nuevo");
        nuevoUsuario.setNombreUsuario(request.getCorreo()); 
        nuevoUsuario.setRol("ADMIN");
        
        // 🛡️ REGLA SAAS: El usuario DEBE nacer con una empresa asignada.
        // Lo asignamos a Tornimax por defecto para que no falle la BD.
        nuevoUsuario.setIdEmpresaTenant("TORNIMAX_001");

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok("Usuario registrado correctamente en IntegraCore");
    }
}