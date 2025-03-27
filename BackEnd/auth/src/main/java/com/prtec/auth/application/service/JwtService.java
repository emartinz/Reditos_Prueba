package com.prtec.auth.application.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.prtec.auth.application.exceptions.GenerateTokenException;
import com.prtec.auth.application.utils.JwtUtil;
import com.prtec.auth.domain.model.entities.User;

/**
 * Servicio para gestionar operaciones de JWT(JSON Web Token)
 * 
 * @author: Edgar Martinez
 * @version: 1.1
 */
@Service
public class JwtService { 
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Constructor que inicializa los servicios.
     */
    public JwtService(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Metodo para generar token
     * @param userDetails
     * @return
     */
    public String generateToken(UserDetails userDetails) {
        logger.info("Generando token para usuario: {}", userDetails.getUsername());
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            throw new GenerateTokenException("Usuario no encontrado: " + userDetails.getUsername());
        }
        
        // Agregar informacion al token en el claim
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userOptional.get().getId());
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList());

        // Generar Token
        return jwtUtil.createToken(claims, userDetails.getUsername());
    }
}