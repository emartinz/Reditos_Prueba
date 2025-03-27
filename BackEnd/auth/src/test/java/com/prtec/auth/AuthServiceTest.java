package com.prtec.auth;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import com.prtec.auth.application.service.AuthService;
import com.prtec.auth.application.service.JwtService;
import com.prtec.auth.application.service.UserService;
import com.prtec.auth.domain.model.entities.User;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticate_Success() {
        String username = "testUser";
        String password = "password";
        String token = "mockToken";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(token);

        String result = authService.authenticate(username, password);
        assertEquals(token, result);
    }

    @Test
    void testAuthenticate_InvalidCredentials() {
        String username = "testUser";
        String password = "wrongPassword";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid Credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(username, password));
    }

    @Test
    void testRegister_Success() {
        User user = new User();
        user.setUsername("newUser");

        when(userService.saveUser(user)).thenReturn(user);

        User result = authService.register(user);
        assertEquals(user, result);
    }

    @Test
    void testRegister_Failure() {
        User user = new User();
        user.setUsername("errorUser");

        when(userService.saveUser(user)).thenThrow(new RuntimeException("Database Error"));

        assertThrows(ResponseStatusException.class, () -> authService.register(user));
    }
}
