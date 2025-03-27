package com.prtec.tasks.application.utils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String MSG_ERR_TOKEN_EXPIRADO = "Token expirado: {}";
    private static final String MSG_ERR_PROCESAMIENTO = "Error desconocido al procesar el token: {}";
    private final SecretKey key;
    
    /**
     * Constructor que inicializa la clave y el tiempo de expiración.
     * 
     * @param jwtSecret El secreto JWT inyectado desde las propiedades.
     * @param expirationTimeMinutes Tiempo de expiración en minutos, se define en application.yaml.
     */
    public JwtUtil(
        @Value("${jwt.secret}") String jwtSecret
    ) {
        this.key = getSecretKey(jwtSecret);
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
     * Metodo para validar si un token expiró
     * 
     * @param token
     * @return verdadero o false dependiendo si el token está expirado
     */
    public boolean isTokenExpired(String token) {
        return getExpirationFromToken(token).before(new Date());
    }

    // Extraer Informacion
    
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
        try {
            return getPayloadFromToken(token).getExpiration();
        } catch (ExpiredJwtException e) {
            logger.warn(MSG_ERR_TOKEN_EXPIRADO, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error(MSG_ERR_PROCESAMIENTO, e.getMessage());
            throw new IllegalArgumentException(MSG_ERR_PROCESAMIENTO, null);
        }
    } 

    /**
     * Metodo para obtener user id desde el Token
     * @param token
     * @return
     */
    public Long getUserIdFromToken(String token) {
        try {
            return Long.parseLong(
                getPayloadFromToken(token).get("userId").toString()
            );
        } catch (ExpiredJwtException e) {
            logger.warn(MSG_ERR_TOKEN_EXPIRADO, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error(MSG_ERR_PROCESAMIENTO, e.getMessage());
            throw new IllegalArgumentException(MSG_ERR_PROCESAMIENTO, null);
        }
    }

    /**
     * Metodo para extraer usuario del token
     * 
     * @param token
     * @return nombre del usuario
     */
    public String getUsernameFromToken(String token) {
        try {
            return getPayloadFromToken(token).getSubject();
        } catch (ExpiredJwtException e) {
            logger.warn(MSG_ERR_TOKEN_EXPIRADO, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error(MSG_ERR_PROCESAMIENTO, e.getMessage());
            throw new IllegalArgumentException(MSG_ERR_PROCESAMIENTO, null);
        }
    }   

    public List<GrantedAuthority> getRolesFromToken(String token) {
        try {
            List<?> roles = getPayloadFromToken(token).get("roles", List.class);
        
            if (roles == null) {
                return Collections.emptyList();
            }
            
            return roles.stream()
                        .filter(String.class::isInstance) // Filtra solo Strings
                        .map(role -> new SimpleGrantedAuthority((String) role))
                        .collect(Collectors.toList());
        } catch (ExpiredJwtException e) {
            logger.warn(MSG_ERR_TOKEN_EXPIRADO, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error(MSG_ERR_PROCESAMIENTO, e.getMessage());
            throw new IllegalArgumentException(MSG_ERR_PROCESAMIENTO, null);
        }
    }
    
    public boolean isTokenValid(String token, String currentUsername) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            
            final String tokenUsername = getUsernameFromToken(token);
            boolean isValid = tokenUsername.equals(currentUsername) && !isTokenExpired(token);
            logger.info("Validación de token para usuario {}: {}", tokenUsername, isValid ? "Valid" : "Invalid");
            return isValid;
            
        } catch (Exception e) {
            logger.error("No se pudo validar el token: {}", e.getMessage());
            return false;
        }
    }
}