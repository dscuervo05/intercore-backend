package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_usuario;
    
    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(name = "contrasena_hash", nullable = false)
    private String contrasenaHash;

    private String nombre;
    
    private String rol; // Ejemplo: "ADMIN", "CAJERO"

    // ===========================================================
    // MÉTODOS OBLIGATORIOS DE USERDETAILS (PASO 4)
    // ===========================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Esto le dice a Spring qué permisos tiene el usuario
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol));
    }

    @Override
    public String getPassword() {
        // Retornamos el campo donde guardamos la clave encriptada
        return this.contrasenaHash;
    }

    @Override
    public String getUsername() {
        // En tu caso, el "username" para el login será el correo
        return this.correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // La cuenta no ha expirado
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // La cuenta no está bloqueada
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Las credenciales no han vencido
    }

    @Override
    public boolean isEnabled() {
        return true; // El usuario está activo
    }
}