package com.prtec.tasks.application.utils;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;

import com.prtec.tasks.application.exceptions.TokenException;
import com.prtec.tasks.application.service.TaskService;
import com.prtec.tasks.domain.model.entity.Task;

/**
 * Utilidad para reducir complejidad de los Controladores
 * @author Edgar Andres
 * @version 1.0
 */
public class AuthUtils {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_ROLE_NAME = "admin";

    private AuthUtils(){}

    /**
     * Método para obtener el token desde el Authorization header
     * @param authHeader
     * @return
     */
    public static ResponseEntity<String> getTokenFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token no autorizado.");
        }
        return ResponseEntity.ok(authHeader.replace(BEARER_PREFIX, ""));
    }

    /**
     * Método para validar si el rol está presente en el token
     * @param token
     * @param role
     * @param jwtUtil
     * @return
     */
    public static boolean hasRole(String token, String role, JwtUtil jwtUtil) {
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        if (roles == null) { return false; }
    
        String roleLowerCase = role.toLowerCase();
        return roles.stream().anyMatch(r -> r.getAuthority().toLowerCase().equals(roleLowerCase));
    }

    /**
     * Método que verifica si el token corresponde al propietario de la tarea.
     * 
     * @param authHeader Encabezado de autorización con el token.
     * @param taskId Id de area que se quiere verificar.
     * @param jwtUtil Utilidad para extraer el userId del token.
     * @param taskService Utilidad para consultar tareas
     * @return true si el token corresponde al propietario de la tarea, false en caso contrario.
     */
    public static boolean isTaskOwnedByUser(String authHeader, Long taskId, JwtUtil jwtUtil, TaskService taskService) {
        return isTaskOwnedByUser(authHeader, taskId, jwtUtil, taskService, false);
    }


    /**
     * Método que verifica si el token corresponde al propietario de la tarea.
     * 
     * @param authHeader Encabezado de autorización con el token.
     * @param taskId Id de area que se quiere verificar.
     * @param jwtUtil Utilidad para extraer el userId del token.
     * @param taskService Utilidad para consultar tareas
     * @param adminIsAllowed Cambia el comportamiento y si es true con solo ser admin retorna true
     * @return true si el token corresponde al propietario de la tarea, false en caso contrario.
     */
    public static boolean isTaskOwnedByUser(String authHeader, Long taskId, JwtUtil jwtUtil, TaskService taskService, boolean adminIsAllowed) {
        // Obtener el token del encabezado de autorización
        ResponseEntity<String> tokenResponse = getTokenFromAuthHeader(authHeader);
    
        // Si el token es inválido o está ausente, retorna false
        if (tokenResponse.getStatusCode() == HttpStatus.UNAUTHORIZED || tokenResponse.getBody() == null) {
            return false;
        }
    
        String token = tokenResponse.getBody();

        // Si adminIsAllowed es true, verifica si el usuario tiene rol de admin y si lo es permite el cambio
        if (adminIsAllowed && hasRole(token, ADMIN_ROLE_NAME, jwtUtil)) {
            return true;
        }

        //Si no es admin continua con la validacion de propiedad de la tarea.

        // Extraer el userId del token
        Long userId = jwtUtil.getUserIdFromToken(token);
    
        // Obtener la tarea existente utilizando taskId
        Task existingTask = taskService.getTaskById(taskId);
        if (existingTask == null) {
            return false; // Tarea no encontrada
        }
    
        // Verificar si el userId del token coincide con el userId de la tarea
        return userId != null && userId.equals(existingTask.getUserDetails().getId());
    }


    /**
     * Metodo que valida si un usuario es Administrador
     * @param authHeader
     * @param jwtUtil
     * @return boolean
     */
    public static boolean isAdminUser(String authHeader, JwtUtil jwtUtil) throws TokenException {
        return validateUserRole(authHeader, jwtUtil, ADMIN_ROLE_NAME);
    }

    /**
     * Metodo que valida si un usuario es Administrador
     * @param authHeader
     * @param jwtUtil
     * @param requiredRole
     * @return boolean
     */
    public static boolean validateUserRole(String authHeader, JwtUtil jwtUtil, String requiredRole) {
        // Obtener el token del encabezado de autorización
        ResponseEntity<String> tokenResponse = getTokenFromAuthHeader(authHeader);
            
        // Si el token es inválido o está ausente, retorna false
        if (tokenResponse.getStatusCode() == HttpStatus.UNAUTHORIZED || tokenResponse.getBody() == null) {
            throw new TokenException("Token no autorizado.");
        }

        String token = tokenResponse.getBody();
        return hasRole(token, requiredRole, jwtUtil);
    }
}