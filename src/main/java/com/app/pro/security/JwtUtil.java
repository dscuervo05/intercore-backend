package com.app.pro.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtUtil {
    
    // 🛡️ Llave estática: Así los tokens no mueren cuando reinicias NetBeans
    private static final String SECRET_KEY_STRING = "IntegraCoreSecretKeyParaTornimaxJYM2026SuperSeguraYPrivada";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    // 🎯 CORREGIDO: Ahora recibe el UserDetails y el idEmpresa
    public String generateToken(UserDetails userDetails, String idEmpresa) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idEmpresa", idEmpresa); // Guardamos la empresa adentro
        return createToken(claims, userDetails.getUsername());
    }
    
    // 🎯 CORREGIDO: El nombre del método y el nivel de acceso (private)
    private String createToken(Map<String, Object> claims, String subject){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new java.util.Date(System.currentTimeMillis()))
                // Expira en 10 horas
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SECRET_KEY)
                .compact();
    }
    
    // 🎯 CORREGIDO: Error de tipeo de "clamis" a "claims"
    public String extractIdEmpresa(String token){
        return extractClaim(token, claims -> claims.get("idEmpresa", String.class));
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}