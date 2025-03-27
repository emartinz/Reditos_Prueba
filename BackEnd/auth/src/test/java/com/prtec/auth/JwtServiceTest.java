package com.prtec.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.prtec.auth.application.exceptions.GenerateTokenException;
import com.prtec.auth.application.service.JwtService;
import com.prtec.auth.application.service.UserService;
import com.prtec.auth.application.utils.JwtUtil;
import com.prtec.auth.domain.model.entities.User;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    
    @Mock
    private UserService userService;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private JwtService jwtService;
    
    private UserDetails userDetails;
    private User domainUser;
    
    @BeforeEach
    void setUp() {
        userDetails = new org.springframework.security.core.userdetails.User("testuser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        domainUser = new User(1L, "testuser", "password");
    }
    
    @Test
    void generateToken_Success() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(domainUser));
        when(jwtUtil.createToken(anyMap(), eq("testuser"))).thenReturn("mocked_token");
        
        String token = jwtService.generateToken(userDetails);
        
        assertNotNull(token);
        assertEquals("mocked_token", token);
    }
    
    @Test
    void generateToken_UserNotFound() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());
        
        assertThrows(GenerateTokenException.class, () -> jwtService.generateToken(userDetails));
    }
}
