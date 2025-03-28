package com.prtec.tasks.adapter.in.controller;

import com.prtec.tasks.application.service.TaskService;
import com.prtec.tasks.application.service.UserDetailsService;
import com.prtec.tasks.application.utils.AuthUtils;
import com.prtec.tasks.application.utils.JwtUtil;
import com.prtec.tasks.domain.model.dto.ApiResponseDTO;
import com.prtec.tasks.domain.model.entity.Task;
import com.prtec.tasks.domain.model.entity.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Controller", description = "Controlador para la gestión de tareas.")
public class TaskController {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String MSG_TASK_NOT_FOUND = "Tarea no encontrada";
    private final TaskService taskService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Operation(summary = "Crear una nueva tarea", responses = {
        @ApiResponse(
            responseCode = "200", description = "Tarea creada exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ), @ApiResponse(
            responseCode = "401", description = "No autorizado",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        )
    })
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<Task>> createTask(@RequestHeader("Authorization") String authHeader, @RequestBody Task task) {
        ResponseEntity<String> tokenResponse = AuthUtils.getTokenFromAuthHeader(authHeader);

        // Si el token es inválido, retorna "No autorizado"
        if (tokenResponse.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, tokenResponse.getBody(), null));
        }

        String token = tokenResponse.getBody();
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);

        UserDetails userDetails = userDetailsService.findOrCreateUser(userId, username);
        task.setUserDetails(userDetails);
        Task createdTask = taskService.createTask(task);
        return ResponseEntity.ok(new ApiResponseDTO<>(ApiResponseDTO.Status.SUCCESS, "Tarea creada", createdTask));
    }


    @Operation(summary = "Actualizar una tarea existente", responses = {
        @ApiResponse(
            responseCode = "200", description = "Tarea actualizada exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "404", description = MSG_TASK_NOT_FOUND,
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "401", description = "No autorizado",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        )
    })
    @SecurityRequirement(name = "Authorization")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Task>> updateTask(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable Long id, 
        @RequestBody Task task
    ) {
        // Verificar que la tarea pertenece al usuario autenticado
        if (!AuthUtils.isTaskOwnedByUser(authHeader, id, jwtUtil, taskService)) {
            // Si el usuario no es el propietario de la tarea
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "No autorizado para actualizar esta tarea", null));
        }

        // Si la validación pasa, se actualiza la tarea
        Task updatedTask = taskService.updateTask(id, task);
        return updatedTask != null
                ? ResponseEntity.ok(new ApiResponseDTO<>(ApiResponseDTO.Status.SUCCESS, "Tarea actualizada", updatedTask))
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "Ocurrio un error al actualizar la tarea.", null));
    }


    @Operation(summary = "Eliminar una tarea por ID", responses = {
        @ApiResponse(
            responseCode = "200", description = "Tarea eliminada exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "404", description = MSG_TASK_NOT_FOUND,
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "401", description = "No autorizado",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        )
    })
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTask(
        @RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        // Verificar que la tarea pertenece al usuario autenticado
        if (!AuthUtils.isTaskOwnedByUser(authHeader, id, jwtUtil, taskService, true)) {
            // Si el usuario no es el propietario de la tarea
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "No autorizado para borrar esta tarea", null));
        }

        boolean isDeleted = taskService.deleteTask(id);
        if (isDeleted) {
            return ResponseEntity.ok(new ApiResponseDTO<>(ApiResponseDTO.Status.SUCCESS, "Tarea eliminada", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, MSG_TASK_NOT_FOUND, null));
        }
    }


    @Operation(summary = "Obtener todas las tareas, solo usuarios con rol de administrador pueden usarla", responses = {
        @ApiResponse(
            responseCode = "200", description = "Lista de tareas obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "401", description = "No autorizado",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        )
    })
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/admin/getAll")
    public ResponseEntity<ApiResponseDTO<Page<Task>>> getAllTasksForAdmin(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) 
    {
        // Verificar si el usuario tiene el rol "admin" usando AuthUtils
        if (!AuthUtils.isAdminUser(authHeader, jwtUtil)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "No tienes permiso para acceder a esta acción", null));
        }

        // Si es admin, obtener todas las tareas con paginación
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Task> tasks = taskService.getAllTasks(pageRequest);

        return ResponseEntity.ok(new ApiResponseDTO<>(ApiResponseDTO.Status.SUCCESS, "Lista de tareas", tasks));
    }


    @Operation(summary = "Obtener todas las tareas del usuario", responses = {
        @ApiResponse(
            responseCode = "200", description = "Lista de tareas obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "401", description = "No autorizado",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        )
    })
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/getAll")
    public ResponseEntity<ApiResponseDTO<Page<Task>>> getAllTasksForUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Extraer el token del encabezado Authorization
        ResponseEntity<String> tokenResponse = AuthUtils.getTokenFromAuthHeader(authHeader);

        // Si el token es inválido, retorna "No autorizado"
        if (tokenResponse.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, tokenResponse.getBody(), null));
        }

        String token = tokenResponse.getBody();

        // Verificar si el usuario tiene el rol "user" usando AuthUtils
        if (!AuthUtils.hasRole(token, "user", jwtUtil)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "No tienes permiso para acceder a esta acción", null));
        }

        // Obtener las tareas del usuario con paginación
        String username = jwtUtil.getUsernameFromToken(token);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Task> tasks = taskService.getTasksByUser(username, pageRequest);

        return ResponseEntity.ok(new ApiResponseDTO<>(ApiResponseDTO.Status.SUCCESS, "Tus tareas", tasks));
    }


    @Operation(summary = "Obtener una tarea por ID", responses = {
        @ApiResponse(
            responseCode = "200", description = "Tarea encontrada exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "404", description = MSG_TASK_NOT_FOUND,
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "401", description = "No autorizado",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        )
    })
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Task>> getTaskById(
        @RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        // Verificar que la tarea pertenece al usuario autenticado
        if (!AuthUtils.isTaskOwnedByUser(authHeader, id, jwtUtil, taskService, true)) {
            // Si el usuario no es el propietario de la tarea
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "No autorizado para ver esta tarea", null));
        }

        Task task = taskService.getTaskById(id);
        return task != null
                ? ResponseEntity.ok(new ApiResponseDTO<>(ApiResponseDTO.Status.SUCCESS, "Tarea encontrada", task))
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, MSG_TASK_NOT_FOUND, null));
    }


    @Operation(summary = "Filtrar tareas por estado, prioridad y título", responses = {
        @ApiResponse(
            responseCode = "200", description = "Lista de tareas filtradas obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "401", description = "No autorizado",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
        )
    })
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/filter")
    public ResponseEntity<ApiResponseDTO<Page<Task>>> getTasksFilteredForUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(ApiResponseDTO.Status.ERROR, "No autorizado", null));
        }

        // Obtener el token y el ID del usuario
        String token = authHeader.replace(BEARER_PREFIX, "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // Crear un objeto PageRequest con los parámetros de paginación
        PageRequest pageRequest = PageRequest.of(page, size);

        // Obtener las tareas filtradas con paginación
        Page<Task> tasks = taskService.getTasksByFilters(userId, title, status, priority, pageRequest);

        // Retornar las tareas filtradas con paginación
        return ResponseEntity.ok(new ApiResponseDTO<>(ApiResponseDTO.Status.SUCCESS, "Lista de tareas filtradas", tasks));
    }
}