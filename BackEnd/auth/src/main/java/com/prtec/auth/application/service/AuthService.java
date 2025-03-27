package com.prtec.auth.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.prtec.auth.domain.model.entities.User;

/**
 * Servicio para operaciones de Autenticacion
 * 
 * @author: Edgar Martinez
 * @version: 1.0
 */
@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserService userService;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /** 
     * Metodo para autenticar usuarios
     * 
     * @param username
     * @param password
     * @return String (token JWT)
     */
    public String authenticate(String username, String password) {
        try {
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            
            if (authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                return jwtService.generateToken(userDetails);
            } else {
                throw new BadCredentialsException("Invalid Credentials");
            }
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid Credentials", e);
        }
    }

    /**
     * Metodo para registrar usuarios
     * 
     * @param user
     * @return User
     */
    public User register(User user) {
        try {
            return userService.saveUser(user);
        } catch (Exception e) {
            // Loguear el error para más detalles
            logger.error("Error al registrar usuario: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al registrar usuario", e);
        }
    }
}