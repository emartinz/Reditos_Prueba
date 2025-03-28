package com.prtec.tasks.config;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.prtec.tasks.application.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de autenticación JWT.
 *
 * Se encarga de interceptar las solicitudes HTTP para verificar
 * la validez de los tokens JWT. Si el token es válido, establece la autenticación
 * en el contexto de seguridad de Spring Security.
 *
 * <p>Este filtro realiza las siguientes acciones:</p>
 * <ul>
 *     <li>Extrae el token JWT del encabezado Authorization.</li>
 *     <li>Valida el token utilizando JwtUtil.</li>
 *     <li>Carga los roles del usuario desde el token.</li>
 *     <li>Establece la autenticación en el contexto de seguridad de Spring si el token es válido.</li>
 * </ul>
 *
 * <p>Si el token es inválido o no está presente, el filtro simplemente pasa la solicitud
 * al siguiente filtro sin establecer autenticación.</p>
 *
 * @author Edgar Andres
 * @version 1.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Permitir que el navegador se comunique con el backend en caso de solicitudes preflight (OPTIONS)
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);   // OK para preflight
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200"); // Origen permitido
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Métodos permitidos
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type"); // Encabezados permitidos
            response.setHeader("Access-Control-Allow-Credentials", "true");   // Permite credenciales
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("No se encontro un token de autorizacion válido.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (!jwtUtil.isTokenValid(token, username)) {
                log.warn("Token inválido para: {}", username);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token inválido o expirado");
                return;
            }

            List<GrantedAuthority> authorities = jwtUtil.getRolesFromToken(token);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}