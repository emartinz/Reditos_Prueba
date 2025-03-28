package com.prtec.tasks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;

import com.prtec.tasks.application.utils.JwtUtil;

import io.jsonwebtoken.Claims;

class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @Mock
    private Claims claims;
    
    private final String jwtSecret = "misuperarchirencontrahipermegaextremaclavee";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtil = new JwtUtil(jwtSecret);
    }
    
    @Test
    void testGetUsernameFromToken() {
        // Aquí puedes utilizar un token predefinido, que tengas disponible, ya que no se está creando uno.
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTc0MzE2Mzc2MSwiZXhwIjoxNzc0Njk5NzYxfQ.djxi9SiiNDIUQnWkJdIlMgLTe8UTv2SrSUrve9i4e6s";
        assertEquals("testUser", jwtUtil.getUsernameFromToken(token));
    }
    
    @Test
    void testGetRolesFromTokenWithValidRoles() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJVU0VSIiwiQURNSU4iXSwic3ViIjoidGVzdFVzZXIiLCJpYXQiOjE3NDMxNjM4MjUsImV4cCI6MTc3NDY5OTgyNX0.1ku2vfBCLdR6YN6xCQ1FIDoXssw7mUlNqJzRNJzQWM8"; 
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(role -> role.getAuthority().equals("USER")));
        assertTrue(roles.stream().anyMatch(role -> role.getAuthority().equals("ADMIN")));
    }
    
    @Test
    void testGetRolesFromTokenWithNoRoles() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTc0MzE2Mzg2NywiZXhwIjoxNzc0Njk5ODY3fQ.I5XQ2jq58uVb49R1awvWWgxzx8l3Q2gfV9JToP6sLFE";
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        assertTrue(roles.isEmpty());
    }
    
    @Test
    void testGetRolesFromTokenWithInvalidRoles() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WzEyMyx0cnVlLCJVU0VSIl0sInN1YiI6InRlc3RVc2VyIiwiaWF0IjoxNzQzMTYzOTAwLCJleHAiOjE3NzQ2OTk5MDB9.a7VVT9IjWheF1f5259FeYfO8PmJ7Ibs0eDVn4UC_yE0";
        List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(token);
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0).getAuthority());
    }
    
    @Test
    void testIsTokenExpired() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTc0MzE2Mzk4OCwiZXhwIjoxNzc0Njk5OTg4fQ.pSSy9NZwFZ_EcsE26E5ziD4z-rp3qEAQ92BI11qlbOI";
        assertFalse(jwtUtil.isTokenExpired(token));
    }
    
    @Test
    void testIsTokenValid() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTc0MzE2MzcyNiwiZXhwIjoxNzc0Njk5NzI2fQ.h5kyxeiPoyD3xWelg4x-OP8kXJB0Bg_E0njP1WCTl_A";
        assertTrue(jwtUtil.isTokenValid(token, "testUser"));
        assertFalse(jwtUtil.isTokenValid(token, "wrongUser"));
    }
}