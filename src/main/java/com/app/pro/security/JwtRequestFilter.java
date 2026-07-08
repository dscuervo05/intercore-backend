package com.app.pro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String correo = null; // Cambiamos el nombre de la variable para que sea más claro (Subject = Correo)
        String jwt = null;

        // 1. Verificar si el token viene en el formato "Bearer [TOKEN]"
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            // CORREGIDO: Usamos extractSubject en lugar de extractUsername
            correo = jwtUtil.extractUsername(jwt); 
        }

        // 2. Si hay un correo pero no está autenticado en el contexto actual de Spring
        if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Buscamos los detalles en tu base de datos a través del correo
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(correo);

            // 3. Validar el token contra la base de datos
            if (jwtUtil.validateToken(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 4. Establecer la autenticación (Dar el paso libre en la API)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Continuar con el siguiente eslabón de la cadena de filtros
        chain.doFilter(request, response);
    }
}