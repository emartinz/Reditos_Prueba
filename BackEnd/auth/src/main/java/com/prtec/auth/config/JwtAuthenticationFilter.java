package com.prtec.auth.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.prtec.auth.application.utils.JwtUtil;

import org.springframework.util.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Se encarga de interceptar las solicitudes HTTP para verificar
 * la validez de los tokens JWT. Si el token es válido, establece la autenticación
 * en el contexto de seguridad de Spring Security.
 *
 * <p>Este filtro realiza las siguientes tareas:
 * <ul>
 *     <li>Extrae el token JWT del encabezado Authorization.</li>
 *     <li>Valida el token utilizando JwtService.</li>
 *     <li>Carga los detalles del usuario desde UserDetailsService.</li>
 *     <li>Establece la autenticación en el contexto de seguridad si el token es válido.</li>
 * </ul>
 *
 * <p>Si el token es inválido o no está presente, el filtro continúa con la
 * cadena de filtros sin establecer autenticación.
 *
 * @author Edgar Andres
 * @version 1.0
 */
@Component
@Lazy
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {     

        // Permitir que el navegador se comunique con el backend en caso de solicitudes preflight (OPTIONS)
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK); // OK para preflight
        
            // Obtener el origen de la solicitud
            String origin = request.getHeader("Origin");
        
            // Lista de orígenes permitidos
            List<String> allowedOrigins = Arrays.asList(
                "http://localhost:4200", "http://localhost", "http://host.docker.internal"
            );
        
            // Si el origen de la solicitud está en la lista, lo permitimos
            if (allowedOrigins.contains(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
            }
        
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Allow-Credentials", "true"); // Permitir credenciales
        
            return;
        }

        String requestURI = request.getRequestURI();
        // Rutas públicas excluidas de este filtro
        if (requestURI.startsWith("/api/register") || 
            requestURI.startsWith("/auth") || 
            requestURI.startsWith("/swagger") || 
            requestURI.startsWith("/v3/api-docs")) {
            
            filterChain.doFilter(request, response);
            return;
        }

        final String username;

        // Valida si request tiene token, si no lo tiene, permite que el SecurityConfig continue
        // con las demas validaciones de seguridad 
        final String token = getTokenFromRequest(request);
        if (token==null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Obtiene usuario desde el token
        username=jwtUtil.getUsernameFromToken(token);

        // Verifica si el username fue extraído del token y no hay una autenticación previa en el contexto de seguridad
        if (username!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
            UserDetails userDetails=userDetailsService.loadUserByUsername(username);
            
            // Valida si el token es válido para el usuario
            if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
                // Crea un objeto de autenticación con los detalles del usuario y sus autoridades (roles)
                UsernamePasswordAuthenticationToken authToken= new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                
                // Agrega detalles adicionales de la solicitud (como la IP o el navegador) a la autenticación
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Establece la autenticación en el contexto de seguridad para la solicitud actual
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                throw new SecurityException("Token inválido");
            }
        }
        
        // Continúa con el siguiente filtro en la cadena, permitiendo que la solicitud avance
        filterChain.doFilter(request, response);
    }

    /**
     * Metodo para extraer el token del request
     * @param request
     * @return
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader=request.getHeader(HttpHeaders.AUTHORIZATION);

        if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }
}