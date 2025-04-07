package com.prtec.tasks.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.prtec.tasks.application.utils.YamlUtil;

import jakarta.annotation.PostConstruct;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "custom.security.cors")
public class CorsConfig implements WebMvcConfigurer {

	// Propiedades cargadas desde YAML (custom.security.cors)
	private List<String> allowedOrigins;
	private List<String> allowedMethods;
	private List<String> allowedHeaders;

	// Propiedades limpias para usar en la configuración
	private String[] allowedOriginsClean;
	private String[] allowedMethodsClean;
	private String[] allowedHeadersClean;

	@PostConstruct
	public void cleanProperties() {
		allowedOriginsClean = YamlUtil.cleanMultilineProperty(allowedOrigins);
		allowedMethodsClean = YamlUtil.cleanMultilineProperty(allowedMethods);
		allowedHeadersClean = YamlUtil.cleanMultilineProperty(allowedHeaders);
	}

	@Override
	public void addCorsMappings(@NonNull CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins(allowedOriginsClean)
				.allowedMethods(allowedMethodsClean)
				.allowedHeaders(allowedHeadersClean)
				.exposedHeaders("Authorization")
				.allowCredentials(true)
				.maxAge(3600L); // 1 hora de cache para las respuestas preflight
	}
}