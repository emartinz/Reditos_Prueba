package com.prtec.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;

import com.prtec.auth.application.utils.JwtUtil;

import io.jsonwebtoken.Claims;

class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @Mock
    private Claims claims;
    
    private final String jwtSecret = "supersecretkeythatisverysecureandlongenough";
    private final long expirationTimeMinutes = 60;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtil = new JwtUtil(jwtSecret, expirationTimeMinutes);
    }
    
    @Test
    void testCreateToken() {
        String token = jwtUtil.createToken(Map.of("userId", "123"), "testUser");
        assertNotNull(token);
    }
    
    @Test
    void testGetUsernameFromToken() {
        String token = jwtUtil.createToken(Map.of(), "testUser");
        assertEquals("testUser", jwtUtil.getUsernameFromToken(token));
    }
    
    @Test
    void testGetRolesFromTokenWithValidRoles() {
        String token = jwtUtil.createToken(Map.of("roles", List.of("ROLE_USER", "ROLE_ADMIN")), "testUser");
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(role -> role.getAuthority().equals("ROLE_USER")));
        assertTrue(roles.stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN")));
    }
    
    @Test
    void testGetRolesFromTokenWithNoRoles() {
        String token = jwtUtil.createToken(Map.of(), "testUser");
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        assertTrue(roles.isEmpty());
    }
    
    @Test
    void testGetRolesFromTokenWithInvalidRoles() {
        String token = jwtUtil.createToken(Map.of("roles", List.of(123, true, "ROLE_USER")), "testUser");
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0).getAuthority());
    }
    
    @Test
    void testIsTokenExpired() {
        String token = jwtUtil.createToken(Map.of(), "testUser");
        assertFalse(jwtUtil.isTokenExpired(token));
    }
    
    @Test
    void testIsTokenValid() {
        String token = jwtUtil.createToken(Map.of(), "testUser");
        assertTrue(jwtUtil.isTokenValid(token, "testUser"));
        assertFalse(jwtUtil.isTokenValid(token, "wrongUser"));
    }
}
