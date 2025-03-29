package com.prtec.tasks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v2/api-docs/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/swagger-ui/**",
                    "/webjars/**"
                ).permitAll()
                // Permitir todas las peticiones OPTIONS a cualquier ruta (Preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/api/tasks/**").hasAuthority("USER")
                .anyRequest().authenticated()
            )
            // Personalizacion para poder mostrar que no cumple con el rol requerido
            .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
            .sessionManagement(sessionManager -> sessionManager
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}