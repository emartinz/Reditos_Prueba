package com.prtec.auth;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.prtec.auth.application.service.AuthService;
import com.prtec.auth.domain.model.dto.ApiResponseDTO;
import com.prtec.auth.domain.model.dto.AuthRequest;
import com.prtec.auth.domain.model.entities.User;
import com.prtec.auth.adapter.in.controller.AuthController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterSuccess() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");

        when(authService.register(any(User.class))).thenReturn(user);

        ResponseEntity<ApiResponseDTO<User>> response = authController.register(user);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("SUCCESS", response.getBody().getStatus().toString());
        assertEquals("testuser", ((User) response.getBody().getData()).getUsername());
    }

    @Test
    void testRegisterFailure() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");

        when(authService.register(any(User.class))).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<ApiResponseDTO<User>> response = authController.register(user);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ERROR", response.getBody().getStatus().toString());
        assertEquals("Error al registrar el usuario", response.getBody().getMessage());
    }

    @Test
    void testLoginSuccess() {
        AuthRequest authRequest = new AuthRequest("testuser", "password123");
        User mockUser = new User();
        mockUser.setUsername("testuser");

        when(authService.authenticate(authRequest.getUsername(), authRequest.getPassword())).thenReturn("mockToken");

        ResponseEntity<ApiResponseDTO<String>> response = authController.login(authRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SUCCESS", response.getBody().getStatus().toString());
        assertEquals("mockToken", response.getBody().getData());
    }

    @Test
    void testLoginFailure() {
        AuthRequest authRequest = new AuthRequest("testuser", "wrongpassword");

        when(authService.authenticate(authRequest.getUsername(), authRequest.getPassword())).thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<ApiResponseDTO<String>> response = authController.login(authRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("ERROR", response.getBody().getStatus().toString());
        assertEquals("Credenciales incorrectas", response.getBody().getMessage());
    }
}