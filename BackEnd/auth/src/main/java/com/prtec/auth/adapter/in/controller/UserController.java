package com.prtec.auth.adapter.in.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.prtec.auth.application.service.UserService;
import com.prtec.auth.domain.model.dto.ApiResponseDTO;
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
@RequestMapping("api/users")
@Tag(name = "Usuarios", description = "Controlador para operaciones relacionadas con usuarios.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Consultar informacion de un usuario
     * @param username
     * @return
     */
    @Operation(summary = "Obtener un usuario por su nombre de usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ApiResponseDTO.class)) }),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", 
            content = { @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ApiResponseDTO.class)) })
    })
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponseDTO<User>> getUser(@PathVariable String username) {
        try {
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("No se encontró un usuario con nombre: " + username));

            ApiResponseDTO<User> response = new ApiResponseDTO<>(
                ApiResponseDTO.Status.SUCCESS, 
                ApiResponseDTO.OK, 
                user
            );

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            ApiResponseDTO<User> response = new ApiResponseDTO<>(
                ApiResponseDTO.Status.ERROR, 
                "Usuario no encontrado", 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}