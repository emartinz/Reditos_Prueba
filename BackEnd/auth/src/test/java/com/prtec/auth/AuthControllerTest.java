package com.prtec.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import com.prtec.auth.adapter.in.controller.AuthController;
import com.prtec.auth.application.service.AuthService;
import com.prtec.auth.application.utils.AuthUtils;
import com.prtec.auth.application.utils.JwtUtil;
import com.prtec.auth.domain.model.dto.ApiResponseDTO;
import com.prtec.auth.domain.model.dto.AuthRequest;
import com.prtec.auth.domain.model.entities.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private JwtUtil jwtUtil;

    private String authHeader;

    @BeforeEach
    void setUp() {
        authHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJVU0VSIl0sInVzZXJJZCI6Miwic3ViIjoidXNlciIsImlhdCI6MTc0MzEyNzg1OSwiZXhwIjoxNzQzMTMxNDU5fQ.VhVcvUvoCdwcmhgnZl5R82jHqz-PLA_H7c7jxqCRfWQ";
    }

    @Test
    void testRegisterAsUser_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");

        when(authService.register(any(User.class))).thenReturn(user);

        ResponseEntity<ApiResponseDTO<User>> response = authController.registerAsUser(user);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("SUCCESS", response.getBody().getStatus().toString());
        assertEquals("testuser", ((User) response.getBody().getData()).getUsername());
    }

    @Test
    void testRegisterAsUser_Failure() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");

        when(authService.register(any(User.class))).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<ApiResponseDTO<User>> response = authController.registerAsUser(user);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ERROR", response.getBody().getStatus().toString());
        assertEquals("Error al registrar el usuario", response.getBody().getMessage());
    }

    @Test
    void testRegisterAsAdmin_Success() {
        User user = new User();
        user.setUsername("testadminuser");
        user.setPassword("password123");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        when(authUtils.isAdminUser(anyString())).thenReturn(true);
        when(authService.register(any(User.class))).thenReturn(user);

        ResponseEntity<ApiResponseDTO<User>> response = authController.registerAsAdmin(authHeader, user);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("SUCCESS", response.getBody().getStatus().toString());
        assertEquals("testadminuser", ((User) response.getBody().getData()).getUsername());
    }

    @Test
    void testRegisterAsAdmin_Failure() {
        User user = new User();
        user.setUsername("testadminuser");
        user.setPassword("password123");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        when(authUtils.isAdminUser(anyString())).thenReturn(true);
        when(authService.register(any(User.class))).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<ApiResponseDTO<User>> response = authController.registerAsAdmin(authHeader, user);

        assertNotNull(response);
        assertNotNull(response.getBody());
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
        when(jwtUtil.createRefreshToken(any(), eq("testuser"))).thenReturn("mockRefreshToken");

        ResponseEntity<ApiResponseDTO<Map<String, String>>> response = authController.login(authRequest);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SUCCESS", response.getBody().getStatus().toString());
        assertNotNull(response.getBody().getData());
        assertEquals("mockToken", response.getBody().getData().get("accessToken"));
        assertEquals("mockRefreshToken", response.getBody().getData().get("refreshToken"));
    }

    @Test
    void testLoginFailure() {
        AuthRequest authRequest = new AuthRequest("testuser", "wrongpassword");

        when(authService.authenticate(authRequest.getUsername(), authRequest.getPassword())).thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<ApiResponseDTO<Map<String, String>>> response = authController.login(authRequest);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("ERROR", response.getBody().getStatus().toString());
        assertEquals("Credenciales incorrectas", response.getBody().getMessage());
    }
}
