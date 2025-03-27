package com.prtec.auth.adapter.in.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prtec.auth.application.service.AuthService;
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
@Tag(name = "Autenticacion", description = "Controlador para operaciones relacionadas con autenticacion.")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar un nuevo usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
            content = { @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ApiResponseDTO.class)) }),
        @ApiResponse(responseCode = "500", description = "Error al registrar el usuario",
            content = { @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ApiResponseDTO.class)) })
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<User>> register(@RequestBody User user) {
        try {
            User savedUserWithRoles = authService.register(user);
            ApiResponseDTO<User> response = new ApiResponseDTO<>(
                ApiResponseDTO.Status.SUCCESS,
                ApiResponseDTO.OK,
                savedUserWithRoles
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponseDTO<User> response = new ApiResponseDTO<>(
                ApiResponseDTO.Status.ERROR,
                "Error al registrar el usuario",
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Iniciar sesión de un usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
            content = { @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ApiResponseDTO.class)) }),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",
            content = { @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ApiResponseDTO.class)) })
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<String>> login(@RequestBody AuthRequest authRequest) {
        try {
            String token = authService.authenticate(authRequest.getUsername(), authRequest.getPassword());
            if (token != null) {
                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                    ApiResponseDTO.Status.SUCCESS,
                    ApiResponseDTO.OK,
                    token
                );
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                    ApiResponseDTO.Status.ERROR,
                    "Credenciales incorrectas",
                    null
                );
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (BadCredentialsException e) {
            ApiResponseDTO<String> response = new ApiResponseDTO<>(
                ApiResponseDTO.Status.ERROR,
                "Credenciales incorrectas",
                null
            );
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            ApiResponseDTO<String> response = new ApiResponseDTO<>(
                ApiResponseDTO.Status.ERROR,
                "Error en el proceso de autenticación",
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}