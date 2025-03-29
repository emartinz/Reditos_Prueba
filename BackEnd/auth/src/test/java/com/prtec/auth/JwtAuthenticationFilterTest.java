package com.prtec.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.prtec.auth.application.utils.JwtUtil;
import com.prtec.auth.config.JwtAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.lang.reflect.Method;
import java.util.Collections;

class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    private void invokeDoFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws Exception {
        Method method = JwtAuthenticationFilter.class.getDeclaredMethod("doFilterInternal", HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
        method.setAccessible(true);
        method.invoke(jwtAuthenticationFilter, request, response, filterChain);
    }

    @Test
    void testDoFilterInternalWithValidToken() throws Exception {
        String token = "validToken";
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid(token, userDetails.getUsername())).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/algun-endpoint");

        invokeDoFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithInvalidToken() throws Exception {
        String token = "invalidToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.getUsernameFromToken(token)).thenThrow(new IllegalArgumentException("Invalid token"));
        when(request.getRequestURI()).thenReturn("/algun-endpoint");

        Exception exception = assertThrows(Exception.class, () -> invokeDoFilterInternal(request, response, filterChain));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Invalid token", exception.getCause().getMessage());

        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithoutToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/algun-endpoint");
        invokeDoFilterInternal(request, response, filterChain);

        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }
}