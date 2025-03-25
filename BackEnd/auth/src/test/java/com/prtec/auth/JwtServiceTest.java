package com.prtec.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.prtec.auth.application.service.jwt.JwtService;

/**
 * Test para la clase JwtService.
 * 
 * Valida la funcionalidad de generación, extracción y validación de tokens JWT.
 * 
 * @author Edgar Andres
 * @version 1.0
 */
class JwtServiceTest {

    private JwtService jwtService;
    private final String jwtSecret = "Y3VzdG9tU2VjcmV0MTIzNDU2Nzg5MGFiY2RlZmdoaWprbG1ub3BxdA==";
    private final long expirationTimeMinutes = 60;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtSecret, expirationTimeMinutes);
    }

    /**
     * Prueba para validar la generación de un token JWT.
     */
    @Test
    void testGenerateToken() {
        UserDetails userDetails = User.withUsername("testUser")
            .password("password")
            .roles("USER")
            .build();
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token, "El token generado no debe ser nulo");
    }

    /**
     * Prueba para verificar la extracción del nombre de usuario desde un token JWT.
     */
    @Test
    void testExtractUsername() {
        UserDetails userDetails = User.withUsername("testUser")
            .password("password")
            .roles("USER")
            .build();
        String token = jwtService.generateToken(userDetails);
        assertEquals("testUser", jwtService.getUsernameFromToken(token), "El nombre de usuario extraído debe coincidir");
    }

    /**
     * Prueba para validar un token JWT válido.
     */
    @Test
    void testValidateToken() {
        UserDetails userDetails = User.withUsername("testUser")
            .password("password")
            .roles("USER")
            .build();
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails), "El token debe ser válido");
    }

    /**
     * Prueba para validar un token JWT inválido.
     */
    @Test
    void testInvalidToken() {
        UserDetails userDetails = User.withUsername("testUser")
            .password("password")
            .roles("USER")
            .build();
        String invalidToken = "invalid.token.value";
        assertFalse(jwtService.isTokenValid(invalidToken, userDetails), "El token inválido no debe ser aceptado");
    }
}
