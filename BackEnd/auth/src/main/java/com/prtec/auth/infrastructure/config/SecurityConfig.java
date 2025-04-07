package com.prtec.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.prtec.auth.application.utils.YamlUtil;
import com.prtec.auth.infrastructure.security.JwtAuthenticationFilter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

/**
 * Configura la seguridad de la aplicación utilizando Spring Security.
 *
 * <p>Esta clase establece:
 * <ul>
 *     <li>Las reglas de autorización y autenticación.</li>
 *     <li>La integración con el filtro JWT para validar tokens de acceso.</li>
 *     <li>La configuración de CORS y CSRF según las necesidades de la aplicación.</li>
 *     <li>El manejo de excepciones y puntos de entrada no autorizados.</li>
 * </ul>
 *
 * <p>Además, define beans como PasswordEncoder y AuthenticationManager para
 * asegurar el correcto manejo de credenciales y autenticación.</p>
 *
 * @author Edgar Andres
 * @version 2.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    
    @Value("#{'${custom.security.public-paths}'.split(',')}")
    private List<String> rawPublicPaths;

    private String[] publicPaths;

    /**
     * Metodo que depura las rutas especificadas en archivo de configuración
     */
    @PostConstruct
    public void cleanPublicPaths() {
        publicPaths = YamlUtil.cleanMultilineProperty(rawPublicPaths);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Public paths: {}", String.join(", ", publicPaths));
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configure(http))
            .authorizeHttpRequests(authRequest -> authRequest
                .requestMatchers(publicPaths).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sessionManager -> sessionManager
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("No autorizado");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Acceso denegado");
                })
            )
            .build();
    }
}