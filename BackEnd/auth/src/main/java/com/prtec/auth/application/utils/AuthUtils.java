package com.prtec.auth.application.utils;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Utilidad para reducir complejidad de los Controladores
 * @author Edgar Andres
 * @version 1.0
 */
@Component
public class AuthUtils {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_ROLE_NAME = "admin";
    private final JwtUtil jwtUtil;

    public AuthUtils(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Método para obtener el token desde el Authorization header
     * @param authHeader
     * @return
     */
    public ResponseEntity<String> getTokenFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No autorizado");
        }
        return ResponseEntity.ok(authHeader.replace(BEARER_PREFIX, ""));
    }

    /**
     * Método para validar si el rol está presente en el token
     * @param token
     * @param role
     * @return
     */
    public boolean hasRole(String token, String role) {
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        if (roles == null) { return false; }
    
        String roleLowerCase = role.toLowerCase();
        return roles.stream().anyMatch(r -> r.getAuthority().toLowerCase().equals(roleLowerCase));
    }

    /**
     * Metodo que valida si un usuario es Administrador
     * @param authHeader
     * @param jwtUtil
     * @return boolean
     */
    public boolean isAdminUser(String authHeader) {
        return validateUserRole(authHeader, ADMIN_ROLE_NAME);
    }

    /**
     * Metodo que valida si un usuario es Administrador
     * @param authHeader
     * @param jwtUtil
     * @param requiredRole
     * @return boolean
     */
    public boolean validateUserRole(String authHeader, String requiredRole) {
        // Obtener el token del encabezado de autorización
        ResponseEntity<String> tokenResponse = getTokenFromAuthHeader(authHeader);
            
        // Si el token es inválido o está ausente, retorna false
        if (tokenResponse.getStatusCode() == HttpStatus.UNAUTHORIZED || tokenResponse.getBody() == null) {
            return false;
        }

        String token = tokenResponse.getBody();
        return hasRole(token, requiredRole);
    }
}
