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
    private final String jwtSecret = "misuperarchirencontrahipermegaextremaclavee";
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
        System.out.println("testGetUsernameFromToken: " + token);
        assertEquals("testUser", jwtUtil.getUsernameFromToken(token));
    }
    
    @Test
    void testGetRolesFromTokenWithValidRoles() {
        String token = jwtUtil.createToken(Map.of("roles", List.of("USER", "ADMIN")), "testUser");
        System.out.println("testGetRolesFromTokenWithValidRoles: " + token);
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(role -> role.getAuthority().equals("USER")));
        assertTrue(roles.stream().anyMatch(role -> role.getAuthority().equals("ADMIN")));
    }
    
    @Test
    void testGetRolesFromTokenWithNoRoles() {
        String token = jwtUtil.createToken(Map.of(), "testUser");
        System.out.println("testGetRolesFromTokenWithNoRoles: " + token);
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        assertTrue(roles.isEmpty());
    }
    
    @Test
    void testGetRolesFromTokenWithInvalidRoles() {
        String token = jwtUtil.createToken(Map.of("roles", List.of(123, true, "USER")), "testUser");
        System.out.println("testGetRolesFromTokenWithInvalidRoles: " + token);
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0).getAuthority());
    }
    
    @Test
    void testIsTokenExpired() {
        String token = jwtUtil.createToken(Map.of(), "testUser");
        System.out.println("testIsTokenExpired: " + token);
        assertFalse(jwtUtil.isTokenExpired(token));
    }
    
    @Test
    void testIsTokenValid() {
        String token = jwtUtil.createToken(Map.of(), "testUser");
        System.out.println("testIsTokenValid: " + token);
        assertTrue(jwtUtil.isTokenValid(token, "testUser"));
        assertFalse(jwtUtil.isTokenValid(token, "wrongUser"));
    }
}
