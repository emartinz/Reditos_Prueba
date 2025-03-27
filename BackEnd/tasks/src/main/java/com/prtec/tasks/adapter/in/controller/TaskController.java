package com.prtec.tasks.adapter.in.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.prtec.tasks.application.service.TaskService;
import com.prtec.tasks.application.service.UserDetailsService;
import com.prtec.tasks.application.utils.JwtUtil;
import com.prtec.tasks.domain.model.entity.Task;
import com.prtec.tasks.domain.model.entity.UserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @PostMapping("/create")
    public ResponseEntity<Task> createTask(@RequestBody Task task, @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Extraer el token eliminando el prefijo "Bearer "
        String token = authHeader.replace("Bearer ", "");
    
        // Extraer datos del token
        Long userId = jwtUtil.getUserIdFromToken(token); 
        String username = jwtUtil.getUsernameFromToken(token);
    
        // Buscar o crear el usuario
        UserDetails userDetails = userDetailsService.findOrCreateUser(userId, username);

        // Asignar el usuario a la tarea antes de guardarla
        task.setUserDetails(userDetails);

        Task createdTask = taskService.createTask(task);
        return ResponseEntity.ok(createdTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        Task updatedTask = taskService.updateTask(id, task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/getAllTasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Task>> getTasksByStatusAndPriority(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String priority,
        @RequestParam(required = false) String title
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Extraer el token eliminando el prefijo "Bearer "
        String token = authHeader.replace("Bearer ", "");
    
        // Extraer datos del token
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Task> tasks = taskService.getTasksByFilters(userId, title, status, priority);
        return ResponseEntity.ok(tasks);
    }
}
