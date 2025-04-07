package com.prtec.auth.infrastructure.security;

import java.io.IOException;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
 * @version 1.1
 */
@Component
@Lazy
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    @Value("${custom.security.public-paths}")
    private String[] publicPaths;
    
    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, String[] publicPaths) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.publicPaths = publicPaths;
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {     

        String requestURI = request.getRequestURI();
        
        // Verificar si la ruta está en PUBLIC_PATHS
        for (String path : publicPaths) {
            if (path.endsWith("/**")) {
                String basePath = path.substring(0, path.length() - 3);
                if (requestURI.startsWith(basePath)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            } else if (requestURI.equals(path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Valida si request tiene token, si no lo tiene, permite que el SecurityConfig continue
        // con las demas validaciones de seguridad 
        final String token = getTokenFromRequest(request);
        
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token no proporcionado");
            return;
        }

        try {
            final String username = jwtUtil.getUsernameFromToken(token);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    //Pasa filtro con authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token inválido");
                    return;
                }
            }
            
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Error al procesar el token: " + e.getMessage());
        }
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