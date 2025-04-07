package com.prtec.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.prtec.auth.application.utils.JwtUtil;
import com.prtec.auth.infrastructure.security.JwtAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collections;

@SpringBootTest
@TestPropertySource(properties = {"custom.security.public-paths=/api/register,/api/login"})
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

    @Value("${custom.security.public-paths}")
    private String[] publicPaths;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService, publicPaths);
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

        // Mockear HttpServletResponse y PrintWriter
        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        // Llamar al filtro
        invokeDoFilterInternal(request, response, filterChain);

        // Verificar que se haya establecido el código de estado UNAUTHORIZED
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Verificar que se haya escrito el mensaje esperado en la respuesta
        verify(printWriter).write("Error al procesar el token: Invalid token");

        // Verificar que la autenticación no se haya establecido
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithoutToken() throws Exception {
        // Ruta pública, sin token
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/register");  // Ruta pública

        // Mockear HttpServletResponse y PrintWriter
        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        // Invocar el filtro
        invokeDoFilterInternal(request, response, filterChain);

        // Verificar que la autenticación no fue establecida (ya que es una ruta pública)
        verify(securityContext, never()).setAuthentication(any());
        
        // Verificar que se ha invocado el siguiente filtro en la cadena
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithoutTokenForPrivatePath() throws Exception {
        // Ruta privada, sin token
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/algun-otro-endpoint");

        // Mockear HttpServletResponse y PrintWriter
        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        // Invocar el filtro
        invokeDoFilterInternal(request, response, filterChain);

        // Verificar que no se pasó la autenticación y se bloqueó la solicitud
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain, never()).doFilter(request, response);
    }
}