package com.prtec.auth.adapter.in.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.prtec.auth.application.service.RoleService;
import com.prtec.auth.application.service.UserService;
import com.prtec.auth.domain.model.dto.ApiResponseDTO;
import com.prtec.auth.domain.model.entities.Role;
import com.prtec.auth.domain.model.entities.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/users")
@Tag(name = "Usuarios", description = "Controlador para operaciones relacionadas con usuarios.")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
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

    /**
     * Asignar rol a usuario específico
     * @param userId
     * @param roles
     * @return
     */
    @Operation(summary = "Asignar roles a un usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ApiResponseDTO.class)) }),
        @ApiResponse(responseCode = "500", description = "Error al asignar roles",
            content = { @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ApiResponseDTO.class)) })
    })
    @PostMapping("/{userId}/assignRoles")
    public ResponseEntity<ApiResponseDTO<Void>> assignRolesToUser(@PathVariable Long userId, @RequestBody List<Role> roles) {
        try {
            // Llamada al servicio para asignar los roles directamente
            roleService.assignRolesToUser(userId, roles);

            ApiResponseDTO<Void> response = new ApiResponseDTO<>(
                ApiResponseDTO.Status.SUCCESS, 
                ApiResponseDTO.OK, 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponseDTO<Void> response = new ApiResponseDTO<>(
                ApiResponseDTO.Status.ERROR, 
                "Error al asignar roles", 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Asignar roles a usuario
     * @param userId
     * @param roleIds
     * @return
     */
    @Operation(summary = "Establecer roles a un usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ApiResponseDTO.class)) }),
        @ApiResponse(responseCode = "500", description = "Error al establecer roles",
            content = { @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ApiResponseDTO.class)) })
    })
    @PostMapping("/{userId}/setRoles")
    public ResponseEntity<ApiResponseDTO<Void>> setRolesToUser(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        try {
            roleService.setRolesToUser(userId, roleIds);

            ApiResponseDTO<Void> response = new ApiResponseDTO<>(
                ApiResponseDTO.Status.SUCCESS, 
                ApiResponseDTO.OK, 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponseDTO<Void> response = new ApiResponseDTO<>(
                ApiResponseDTO.Status.ERROR, 
                "Error al establecer roles", 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}