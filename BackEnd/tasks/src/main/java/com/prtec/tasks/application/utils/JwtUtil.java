package com.prtec.tasks.application.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prtec.tasks.domain.model.dto.ApiResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 * Utilidad para trabajar con JWT
 * 
 * @author Edgar Martinez
 * @version 1.1
 */
@Component
public class JwtUtil {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
	private static final String USER_ID = "userId";
	private static final String ROLES = "roles";

	@Value("${custom.endpoints.auth:http://localhost:8080}")
	private String authServiceUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * Metodo para extraer payload sin requerir la llave de firma.
	 * Solo decodifica el contenido sin validar la firma.
	 *
	 * @param token JWT en formato estándar
	 * @return Claims decodificados, o vacíos si el token es inválido
	 */
	public Claims getPayloadFromToken(String token) {
		try {
			// Validar estructura básica del token
			String[] parts = token.split("\\.");
			if (parts.length != 3) {
				logger.error("Token inválido: no tiene el formato correcto");
				return Jwts.claims().build();
			}

			// Decodificar el payload
			String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

			// Convertir JSON a Map
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> map = mapper.readValue(payload, new TypeReference<Map<String, Object>>() {
			});

			// Crear un nuevo objeto Claims con el contenido del mapa
			return Jwts.claims()
					.subject(map.get("sub").toString())
					.issuedAt(new Date(((Number) map.get("iat")).longValue() * 1000))
					.expiration(new Date(((Number) map.get("exp")).longValue() * 1000))
					.add(USER_ID, map.get(USER_ID))
					.add(ROLES, map.get(ROLES))
					.build();

		} catch (Exception e) {
			logger.error("Error al extraer el payload del token: {}", e.getMessage());
			return Jwts.claims().build();
		}
	}

	/**
	 * Metodo para extraer fecha de expiracion del token
	 * 
	 * @param token
	 * @return fecha de expiracion del token
	 */
	public Date getExpirationFromToken(String token) {
		return getPayloadFromToken(token).getExpiration();
	}

	/**
	 * Metodo para obtener user id desde el Token
	 * 
	 * @param token
	 * @return
	 */
	public Long getUserIdFromToken(String token) {
		return Long.parseLong(getPayloadFromToken(token).get(USER_ID).toString());
	}

	/**
	 * Metodo para extraer usuario del token
	 * 
	 * @param token
	 * @return nombre del usuario
	 */
	public String getUsernameFromToken(String token) {
		return getPayloadFromToken(token).getSubject();
	}

	/**
	 * Metodo para extraer los roles desde el token
	 * 
	 * @param token
	 * @return List<GrantedAuthority>
	 */
	public List<GrantedAuthority> getRolesFromToken(String token) {
		List<?> roles = getPayloadFromToken(token).get(ROLES, List.class);

		return roles == null ? Collections.emptyList()
				: roles.stream()
						.filter(String.class::isInstance)
						.map(role -> new SimpleGrantedAuthority((String) role))
						.collect(Collectors.toList());
	}

	/**
	 * Metodo para validar si un token es valido consultando al servicio de
	 * autenticación
	 * 
	 * @param token
	 * @param currentUsername
	 * @return boolean
	 */
	public boolean isTokenValid(String token, String currentUsername) {
		try {

			final String tokenUsername = getUsernameFromToken(token);
			boolean isValidUsername = tokenUsername.equals(currentUsername) && !isTokenExpired(token);

			if (!isValidUsername) {
				logger.warn("Usuario inválido en el token: {}", tokenUsername);
				return false;
			}

			String url = authServiceUrl + "/api/verify";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);

			HttpEntity<String> entity = new HttpEntity<>(token, headers);

			ResponseEntity<ApiResponseDTO<Map<String, Object>>> response = restTemplate.exchange(
					url,
					HttpMethod.POST,
					entity,
					new ParameterizedTypeReference<>() {
					});

			ApiResponseDTO<Map<String, Object>> responseBody = response.getBody();
			return response.getStatusCode().is2xxSuccessful() &&
					responseBody != null &&
					responseBody.getStatus() == ApiResponseDTO.Status.SUCCESS &&
					Boolean.TRUE.equals(responseBody.getData().get("valid"));
		} catch (ResourceAccessException e) {
			// Esta excepción suele indicar problemas de conexión, como que el servidor no
			// está disponible
			logger.error("Error de acceso al recurso (posible problema de conexión): {}", e.getMessage());
			// Aquí podrías registrar el error, lanzar una excepción personalizada, o
			// devolver un valor por defecto
			return false;
		} catch (HttpClientErrorException e) {
			// Esta excepción indica errores del lado del cliente (códigos 4xx), incluyendo
			// el 401
			logger.error("Error del cliente ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
			return false;
		} catch (HttpServerErrorException e) {
			// Esta excepción indica errores del lado del servidor (códigos 5xx)
			logger.error("Error del servidor ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
			return false;
		} catch (Exception e) {
			logger.error("No se pudo validar el token: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Metodo para validar si un token expiró
	 * 
	 * @param token
	 * @return verdadero o false dependiendo si el token está expirado
	 */
	public boolean isTokenExpired(String token) {
		return getExpirationFromToken(token).before(new Date());
	}
}