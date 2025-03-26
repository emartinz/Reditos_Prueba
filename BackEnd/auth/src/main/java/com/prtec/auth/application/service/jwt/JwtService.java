package com.prtec.auth.application.service.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Servicio para gestionar operaciones de JWT(JSON Web Token)
 * 
 * @author: Edgar Martinez
 * @version: 1.0
 */
@Service
public class JwtService { 
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private final SecretKey key;
    private final long expirationTime;

    /**
     * Constructor que inicializa la clave y el tiempo de expiración.
     * 
     * @param jwtSecret El secreto JWT inyectado desde las propiedades.
     * @param expirationTimeMinutes Tiempo de expiración en minutos, se define en application.yaml.
     */
    public JwtService(
        @Value("${jwt.secret}") String jwtSecret,
        @Value("${jwt.expiration-time-minutes}") long expirationTimeMinutes
    ) {
        this.key = getSecretKey(jwtSecret);
        this.expirationTime = expirationTimeMinutes * 60 * 1000; // Convertir a milisegundos
        logger.info("JWT Service: Inicialización con {} minutos para expiracion de tokens.", expirationTimeMinutes);
    }

    /**
     * Metodo para convertir secreto de formato String a formato {@link javax.crypto.SecretKey}.
     * 
     * @return llave en formato {@link javax.crypto.SecretKey}.
     */
    private SecretKey getSecretKey(String jwtSecret) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.error("Error al encriptar clave a formato SecretKey: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Metodo para generar tokens
     * @param userDetails
     * @return
     */
    public String generateToken(UserDetails userDetails) {
        logger.info("Generando token para usuario: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Metodo para crear token, es invocado por {@link #generateToken(UserDetails)}
     * 
     * @param claims
     * @param subject
     * @return Token JWT
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // Validaciones

    /**
     * Metodo para validar tokens
     * 
     * @param token
     * @param userDetails
     * @return verdadero o falso dependiendo de si el token es válido o no
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            // Verificar la firma antes de extraer la información
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    
            final String username = getUsernameFromToken(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            logger.info("Validación de token para usuario {}: {}", username, isValid ? "Valid" : "Invalid");
            return isValid;
        } catch (Exception e) {
            logger.error("No se pudo validar el token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Metodo para validar si un token expiró
     * 
     * @param token
     * @return verdadero o false dependiendo si el token está expirado
     */
    private boolean isTokenExpired(String token) {
        return getExpirationFromToken(token).before(new Date());
    }

    /**
     * Metodo para extraer fecha de expiracion del token
     * 
     * @param token
     * @return fecha de expiracion del token
     */
    private Date getExpirationFromToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        } catch (Exception e) {
            throw new IllegalArgumentException("Token inválido o expirado.", e);
        }
    }

    // Extraer Informacion

    /**
     * Metodo para extraer usuario del token
     * 
     * @param token
     * @return nombre del usuario
     */
    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        } catch (Exception e) {
            logger.error("Error desconocido al procesar el token: {}", e.getMessage());
            throw new IllegalArgumentException("Error al procesar el token.", null);
        }
    }    
}