package com.prtec.tasks;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;

import com.prtec.tasks.application.utils.AuthUtils;
import com.prtec.tasks.application.utils.JwtUtil;

import java.util.Arrays;
import java.util.List;

class AuthUtilsTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthUtils authUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTokenFromAuthHeader_ValidToken() {
        String authHeader = "Bearer validToken";
        ResponseEntity<String> response = AuthUtils.getTokenFromAuthHeader(authHeader);
        
        assertEquals("validToken", response.getBody());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetTokenFromAuthHeader_InvalidToken() {
        String authHeader = "invalidToken";
        ResponseEntity<String> response = AuthUtils.getTokenFromAuthHeader(authHeader);
        
        assertEquals("No autorizado", response.getBody());
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testHasRole_RolePresent() {
        String token = "validToken";
        String role = "admin";
        
        GrantedAuthority authority = () -> "admin";
        List<GrantedAuthority> roles = Arrays.asList(authority);
        when(jwtUtil.getRolesFromToken(token)).thenReturn(roles);
        
        boolean result = AuthUtils.hasRole(token, role, jwtUtil);
        
        assertTrue(result);
    }

    @Test
    void testHasRole_RoleNotPresent() {
        String token = "validToken";
        String role = "user";
        
        GrantedAuthority authority = () -> "admin";
        List<GrantedAuthority> roles = Arrays.asList(authority);
        when(jwtUtil.getRolesFromToken(token)).thenReturn(roles);
        
        boolean result = AuthUtils.hasRole(token, role, jwtUtil);
        
        assertFalse(result);
    }

    @Test
    void testIsAdminUser_UserIsAdmin() {
        String authHeader = "Bearer validToken";
        
        GrantedAuthority authority = () -> "admin";
        List<GrantedAuthority> roles = Arrays.asList(authority);
        when(jwtUtil.getRolesFromToken(anyString())).thenReturn(roles);
        
        boolean result = AuthUtils.isAdminUser(authHeader, jwtUtil);
        
        assertTrue(result);
    }

    @Test
    void testIsAdminUser_UserIsNotAdmin() {
        String authHeader = "Bearer validToken";
        
        GrantedAuthority authority = () -> "user";
        List<GrantedAuthority> roles = Arrays.asList(authority);
        when(jwtUtil.getRolesFromToken(anyString())).thenReturn(roles);
        
        boolean result = AuthUtils.isAdminUser(authHeader, jwtUtil);
        
        assertFalse(result);
    }

    @Test
    void testValidateUserRole_UserHasRole() {
        String authHeader = "Bearer validToken";
        String role = "admin";
        
        GrantedAuthority authority = () -> "admin";
        List<GrantedAuthority> roles = Arrays.asList(authority);
        when(jwtUtil.getRolesFromToken(anyString())).thenReturn(roles);
        
        boolean result = AuthUtils.validateUserRole(authHeader, jwtUtil, role);
        
        assertTrue(result);
    }

    @Test
    void testValidateUserRole_UserDoesNotHaveRole() {
        String authHeader = "Bearer validToken";
        String role = "admin";
        
        GrantedAuthority authority = () -> "user";
        List<GrantedAuthority> roles = Arrays.asList(authority);
        when(jwtUtil.getRolesFromToken(anyString())).thenReturn(roles);
        
        boolean result = AuthUtils.validateUserRole(authHeader, jwtUtil, role);
        
        assertFalse(result);
    }
}