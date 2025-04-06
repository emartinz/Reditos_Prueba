package com.prtec.tasks.adapter.in.controller;

import com.prtec.tasks.application.service.UserDetailsService;
import com.prtec.tasks.application.utils.AuthUtils;
import com.prtec.tasks.application.utils.JwtUtil;
import com.prtec.tasks.domain.model.dto.ApiResponseDTO;
import com.prtec.tasks.domain.model.dto.UserDetailsRequestDTO;
import com.prtec.tasks.domain.model.dto.UserDetailsResponseDTO;
import com.prtec.tasks.domain.model.entity.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Details", description = "Gestión de los detalles del usuario")
public class UserDetailsController {

	private final AuthUtils authUtils;
	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	public UserDetailsController(AuthUtils authUtils, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.authUtils = authUtils;
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Operation(summary = "Registrar o actualizar usuario", description = "Guarda o actualiza los detalles de un usuario en la base de datos.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Detalles del usuario", required = true, content = @Content(schema = @Schema(implementation = UserDetails.class))), responses = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario registrado/actualizado exitosamente", content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud incorrecta", content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
	})
	@PostMapping("/registerUser")
	public ResponseEntity<ApiResponseDTO<UserDetails>> saveOrUpdateUserDetails(
			@RequestHeader("Authorization") String authHeader,
			@RequestBody UserDetailsRequestDTO request) {
		try {
			UserDetails userDetails = userDetailsService.saveOrUpdateUserDetails(
					request.getUserId(), request.getUsername(), request.getEmail(),
					request.getFirstName(), request.getLastName());
			return ResponseEntity.ok(
					new ApiResponseDTO<>(ApiResponseDTO.Status.SUCCESS,
							"Usuario registrado/actualizado exitosamente",
							userDetails));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(
					new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "Solicitud incorrecta: " + e.getMessage(), null));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "Error interno del servidor", null));
		}
	}

	@Operation(summary = "Obtener detalles del usuario por ID", description = "Devuelve los detalles básicos del usuario (sin datos sensibles).", responses = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Detalles del usuario encontrados", content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
	})
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponseDTO<UserDetailsResponseDTO>> getUserDetailsById(
			@RequestHeader("Authorization") String authHeader,
			@PathVariable Long id) {
		try {
			// Obtener datos del usuario solicitado
			UserDetails user = userDetailsService.getUserDetailsById(id);
			if (user == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "Usuario no encontrado", null));
			}

			// Si no es admin, verificar que el username del token coincida con el username
			// consultado
			if (!authUtils.isAdminUser(authHeader)) {
				String usernameFromToken = jwtUtil
						.getUsernameFromToken(authUtils.getTokenFromAuthHeader(authHeader).getBody());
				if (!user.getUsername().equals(usernameFromToken)) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN)
							.body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR,
									"No tienes permiso para acceder a esta acción", null));
				}
			}

			// Armar DTO de respuesta
			UserDetailsResponseDTO dto = new UserDetailsResponseDTO(
					user.getUserId(),
					user.getUsername(),
					user.getEmail(),
					user.getFirstName(),
					user.getLastName());

			return ResponseEntity.ok(
					new ApiResponseDTO<>(ApiResponseDTO.Status.SUCCESS, "Usuario encontrado", dto));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "Error al obtener usuario", null));
		}
	}

}