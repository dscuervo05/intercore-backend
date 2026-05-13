package com.app.pro.security;

import com.app.pro.entity.Usuario;
import com.app.pro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("1. Intentando buscar en BD el correo: " + username);
        
        return usuarioRepository.findByCorreo(username).map(usuario -> {
            System.out.println("2. ¡Usuario ENCONTRADO en BD!");
            System.out.println("3. Hash guardado: " + usuario.getContrasenaHash());
            return usuario;
        }).orElseThrow(() -> {
            System.out.println("2. ERROR FATAL: El usuario NO existe en la base de datos.");
            return new UsernameNotFoundException("Usuario no encontrado con el correo: " + username);
        });
    }
}
