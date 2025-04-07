package com.prtec.auth.adapter.in.controller;

import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prtec.auth.application.service.AuthService;
import com.prtec.auth.application.utils.AuthUtils;
import com.prtec.auth.application.utils.JwtUtil;
import com.prtec.auth.domain.model.dto.ApiResponseDTO;
import com.prtec.auth.domain.model.dto.AuthRequest;
import com.prtec.auth.domain.model.entities.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para gestionar peticiones relacionadas con la Autenticacion
 * 
 * @author: Edgar Martinez
 * @version: 1.0
 */
@RestController
@RequestMapping("api")
@Tag(name = "Auth Controller", description = "Controlador para operaciones relacionadas con autenticacion.")
public class AuthController {
	private final AuthService authService;
	private final JwtUtil jwtUtil;
	private final AuthUtils authUtils;

	public AuthController(AuthService authService, JwtUtil jwtUtil, AuthUtils authUtils) {
		this.authService = authService;
		this.jwtUtil = jwtUtil;
		this.authUtils = authUtils;
	}

	@Operation(summary = "Registrar un nuevo usuario")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)) }),
			@ApiResponse(responseCode = "500", description = "Error al registrar el usuario", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)) })
	})
	@PostMapping("/admin/register")
	public ResponseEntity<ApiResponseDTO<User>> registerAsAdmin(@RequestHeader("Authorization") String authHeader,
			@RequestBody User user) {
		try {
			// Verificar si el usuario tiene el rol "admin" usando AuthUtils
			if (!authUtils.isAdminUser(authHeader)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR,
								"No tienes permiso para acceder a esta acción", null));
			}

			User savedUserWithRoles = authService.register(user);
			ApiResponseDTO<User> response = new ApiResponseDTO<>(
					ApiResponseDTO.Status.SUCCESS,
					ApiResponseDTO.OK,
					savedUserWithRoles);
			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (Exception e) {
			return getRegistrationErrorResponse();
		}
	}

	@Operation(summary = "Registrar un nuevo usuario")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)) }),
			@ApiResponse(responseCode = "500", description = "Error al registrar el usuario", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)) })
	})
	@PostMapping("/register")
	public ResponseEntity<ApiResponseDTO<User>> registerAsUser(@RequestBody User user) {
		try {
			User savedUserWithRoles = authService.register(user);
			ApiResponseDTO<User> response = new ApiResponseDTO<>(
					ApiResponseDTO.Status.SUCCESS,
					ApiResponseDTO.OK,
					savedUserWithRoles);
			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (Exception e) {
			return getRegistrationErrorResponse();
		}
	}

	@Operation(summary = "Iniciar sesión de un usuario")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Autenticación exitosa", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Credenciales incorrectas", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)) })
	})
	@PostMapping("/login")
	public ResponseEntity<ApiResponseDTO<Map<String, String>>> login(@RequestBody AuthRequest authRequest) {
		try {
			String accessToken = authService.authenticate(authRequest.getUsername(), authRequest.getPassword());
			if (accessToken != null) {
				// Generar refresh token
				String refreshToken = jwtUtil.createRefreshToken(
						Map.of("username", authRequest.getUsername()),
						authRequest.getUsername());

				return ResponseEntity.ok(new ApiResponseDTO<>(
						ApiResponseDTO.Status.SUCCESS,
						ApiResponseDTO.OK,
						Map.of("accessToken", accessToken, "refreshToken", refreshToken)));
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new ApiResponseDTO<>(
							ApiResponseDTO.Status.ERROR,
							"Credenciales incorrectas",
							null));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new ApiResponseDTO<>(
							ApiResponseDTO.Status.ERROR,
							"Credenciales incorrectas",
							null));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponseDTO<>(
							ApiResponseDTO.Status.ERROR,
							"Error en el proceso de autenticación",
							null));
		}
	}

	@Operation(summary = "Verificar la validez de un token JWT")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Token válido", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Token inválido o expirado", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)) })
	})
	@PostMapping("/verify")
	public ResponseEntity<ApiResponseDTO<Map<String, Object>>> verifyToken(@RequestBody String token) {
		try {
			if (token == null || token.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new ApiResponseDTO<>(
								ApiResponseDTO.Status.ERROR,
								"Token no proporcionado",
								null));
			}

			String username = jwtUtil.getUsernameFromToken(token);
			boolean isValid = jwtUtil.isTokenValid(token, username);
			boolean isRefreshToken = jwtUtil.isRefreshToken(token);
			Date expiration = jwtUtil.getExpirationFromToken(token);

			Map<String, Object> tokenInfo = Map.of(
					"valid", isValid,
					"isRefreshToken", isRefreshToken,
					"username", username,
					"expiration", expiration);

			if (isValid) {
				return ResponseEntity.ok(new ApiResponseDTO<>(
						ApiResponseDTO.Status.SUCCESS,
						"Token válido",
						tokenInfo));
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new ApiResponseDTO<>(
								ApiResponseDTO.Status.ERROR,
								"Token inválido o expirado",
								tokenInfo));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponseDTO<>(
							ApiResponseDTO.Status.ERROR,
							"Error al verificar el token: " + e.getMessage(),
							null));
		}
	}

	private ResponseEntity<ApiResponseDTO<User>> getRegistrationErrorResponse() {
		ApiResponseDTO<User> response = new ApiResponseDTO<>(
				ApiResponseDTO.Status.ERROR,
				"Error al registrar el usuario",
				null);
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}