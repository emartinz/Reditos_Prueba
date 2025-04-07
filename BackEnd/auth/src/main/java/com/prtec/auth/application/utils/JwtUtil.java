package com.prtec.auth.application.utils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.prtec.auth.application.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidad para trabajar con JWT
 * @author Edgar Martinez
 * @version 1.1
 */
@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String TOKEN_TYPE = "tokenType";
    
    private final SecretKey key;
    private final long expirationTime;
    private final long refreshTokenExpirationTime;
    
    /**
     * Constructor que inicializa la clave y el tiempo de expiración.
     * @param jwtProperties Propiedades de JWT inyectadas desde la configuración.
     */
    @Autowired
    public JwtUtil(JwtProperties jwtProperties) {
        this.key = getSecretKey(jwtProperties.getSecret());
        this.expirationTime = jwtProperties.getExpirationTimeMinutes() * 60 * 1000; // Convertir a milisegundos
        this.refreshTokenExpirationTime = jwtProperties.getExpirationTimeMinutes() * 60 * 1000 * 24 * 7; // 7 días
        logger.info("JWT Service: Inicialización con {} minutos para expiración de tokens.", jwtProperties.getExpirationTimeMinutes());
    }

    /**
     * Constructor que inicializa la clave y el tiempo de expiración.
     * 
     * @param jwtSecret El secreto JWT inyectado desde las propiedades.
     * @param expirationTimeMinutes Tiempo de expiración en minutos, se define en application.yaml.
     */
    public JwtUtil(String jwtSecret, long expirationTimeMinutes) {
        this.key = getSecretKey(jwtSecret);
        this.expirationTime = expirationTimeMinutes * 60 * 1000; // Convertir a milisegundos
        this.refreshTokenExpirationTime = expirationTimeMinutes * 60 * 1000 * 24 * 7; // 7 días
        logger.info("JWT Service: Inicialización con {} minutos para expiracion de tokens.", expirationTimeMinutes);
    }


    /**
     * Metodo para convertir secreto de formato String a formato {@link javax.crypto.SecretKey}.
     * 
     * @return llave en formato {@link javax.crypto.SecretKey}.
     */
    private static SecretKey getSecretKey(String secretKey) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al convertir clave JWT.", e);
        }
    }

    /**
     * Metodo para crear token de acceso, es invocado por {@link #generateToken(UserDetails)}
     * 
     * @param claims
     * @param subject
     * @return Token JWT
     */
    public String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Metodo para crear refresh token
     * 
     * @param claims
     * @param subject
     * @return Refresh Token JWT
     */
    public String createRefreshToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
                .signWith(key, Jwts.SIG.HS256)
                .claim(TOKEN_TYPE, "refresh")
                .compact();
    }
    
    /**
     * Metodo para extraer payload
     * @param token
     * @return
     */
    public Claims getPayloadFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Metodo para extraer fecha de expiracion del token
     * 
     * @param token
     * @return fecha de expiracion del token
     */
    public Date getExpirationFromToken(String token) {
        return getPayloadFromToken(token).getExpiration();
    } 

    /**
     * Metodo para obtener user id desde el Token
     * @param token
     * @return
     */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getPayloadFromToken(token).get("userId").toString());
    }

    /**
     * Metodo para extraer usuario del token
     * 
     * @param token
     * @return nombre del usuario
     */
    public String getUsernameFromToken(String token) {
        return getPayloadFromToken(token).getSubject();
    }   

    /**
     * Metodo para extraer los roles desde el token
     * @param token
     * @return List<GrantedAuthority>
     */
    public List<GrantedAuthority> getRolesFromToken(String token) {
        List<?> roles = getPayloadFromToken(token).get("roles", List.class);
    
        return roles == null ? Collections.emptyList() :
                roles.stream()
                    .filter(String.class::isInstance)
                    .map(role -> new SimpleGrantedAuthority((String) role))
                    .collect(Collectors.toList());
    }
    
    /**
     * Metodo para validar si un token es valido
     * @param token
     * @param currentUsername
     * @return boolean
     */ 
    public boolean isTokenValid(String token, String currentUsername) {
        try {
            Claims payload = getPayloadFromToken(token); // Obtener el payload solo una vez
            boolean isExpired = payload.getExpiration().before(new Date());

            if (isExpired) {
                logger.info("Token expirado.");
                return false;
            }
            
            final String tokenUsername = payload.getSubject();
            boolean isValid = tokenUsername.equals(currentUsername);
            logger.info("Validación de token para usuario {}: {}", tokenUsername, isValid ? "Valid" : "Invalid");
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
    public boolean isTokenExpired(String token) {
        return getExpirationFromToken(token).before(new Date());
    }

    /**
     * Método para verificar si un token es un refresh token
     * 
     * @param token
     * @return verdadero si es un refresh token
     */
    public boolean isRefreshToken(String token) {
        Claims claims = getPayloadFromToken(token);
        return claims.get(TOKEN_TYPE) != null && claims.get(TOKEN_TYPE).equals("refresh");
    }
}