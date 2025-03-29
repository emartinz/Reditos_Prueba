package com.prtec.tasks.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, 
                        HttpServletResponse response, 
                        AccessDeniedException accessDeniedException) throws IOException {
        
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "DESCONOCIDO";
        logger.warn("Se denego el acceso al usuario '{}' al recurso: '{}', debido a que no cumple con el rol requerido.", 
                    username, request.getRequestURI());

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso Denegado.");
    }
}