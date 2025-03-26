package com.prtec.auth.application.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.prtec.auth.application.service.jwt.JwtService;
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
    private final UserService userService;
    private final RoleService roleService;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UserService userService, RoleService roleService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.roleService = roleService;
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
        User savedUser = userService.saveUser(user);
        try {
            roleService.assignRolesToUser(savedUser.getId(), user.getRoles());
        } catch (Exception e) {
            // Manejo de errores al asignar roles
            e.printStackTrace();
        }
        return savedUser;
    }
}