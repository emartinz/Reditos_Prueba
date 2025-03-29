package com.prtec.tasks.adapter.in.controller;

import com.prtec.tasks.application.service.UserDetailsService;
import com.prtec.tasks.domain.model.dto.ApiResponseDTO;
import com.prtec.tasks.domain.model.dto.UserDetailsRequestDTO;
import com.prtec.tasks.domain.model.entity.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Details", description = "Gestión de los detalles del usuario")
public class UserDetailsController {

    private final UserDetailsService userDetailsService;

    
    @Operation(
        summary = "Registrar o actualizar usuario",
        description = "Guarda o actualiza los detalles de un usuario en la base de datos.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Detalles del usuario",
            required = true,
            content = @Content(schema = @Schema(implementation = UserDetails.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario registrado/actualizado exitosamente", 
                        content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud incorrecta", 
                        content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor", 
                        content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
        }
    )
    @PostMapping("/registerUser")
    public ResponseEntity<ApiResponseDTO<UserDetails>> saveOrUpdateUserDetails(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UserDetailsRequestDTO request) { 
        try {
            UserDetails userDetails = userDetailsService.saveOrUpdateUserDetails(
                request.getUserId(), request.getUsername(), request.getEmail(),
                request.getFirstName(), request.getLastName()
            );
            return ResponseEntity.ok(
                new ApiResponseDTO<>(ApiResponseDTO.Status.SUCCESS,
                "Usuario registrado/actualizado exitosamente",
                userDetails
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "Solicitud incorrecta: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR,"Error interno del servidor", null));
        }
    }
}