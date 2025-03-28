package com.prtec.tasks.application.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prtec.tasks.adapter.out.repository.ITaskRepository;
import com.prtec.tasks.application.exceptions.TaskNotFoundException;
import com.prtec.tasks.domain.model.entity.Task;

@Service
@RequiredArgsConstructor
public class TaskService {
    public static final String TASK_NOT_FOUND_MESSAGE = "No se encontró una tarea con id: ";
    private final ITaskRepository taskRepository;

    /**
     * Crea una nueva tarea.
     * @param task La tarea a crear.
     * @return La tarea creada.
     */
    @Transactional
    public Task createTask(Task task) {
        if (task.getUserDetails() == null) {
            throw new IllegalArgumentException("La tarea debe tener un usuario asignado.");
        }
        return taskRepository.save(task);
    }

    /**
     * Actualiza una tarea existente.
     * @param id Identificador de la tarea.
     * @param task La información actualizada de la tarea.
     * @return La tarea actualizada.
     */
    @Transactional
    public Task updateTask(Long id, Task task) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_MESSAGE + id));

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setDueDate(task.getDueDate());
        existingTask.setPriority(task.getPriority());

        return taskRepository.save(existingTask);
    }

    /**
     * Elimina una tarea por ID.
     * @param id Identificador de la tarea.
     * @return boolean
     */
    @Transactional
    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;  // La tarea fue eliminada con éxito
        } else {
            return false;  // La tarea no fue encontrada
        }
    }

    /**
     * Obtiene una lista de tareas filtradas por estado y/o prioridad, con soporte a paginación.
     * @param userId ID del usuario
     * @param title Título de la tarea (opcional)
     * @param status Estado de la tarea (opcional)
     * @param priority Prioridad de la tarea (opcional)
     * @param pageable Parámetro de paginación que incluye la página y el tamaño
     * @return Página de tareas filtradas
     */
    public Page<Task> getTasksByFilters(Long userId, String title, String status, String priority, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio.");
        }

        Task.TaskStatus taskStatus = null;
        Task.TaskPriority taskPriority = null;

        if (status != null) {
            try {
                taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("El estado de tarea proporcionado no es válido.");
            }
        }

        if (priority != null) {
            try {
                taskPriority = Task.TaskPriority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("La prioridad proporcionada no es válida.");
            }
        }

        // Devuelve una página de tareas filtradas utilizando el repositorio
        return taskRepository.findByUserAndFilters(userId, title, taskStatus, taskPriority, pageable);
    }

    /**
     * Obtiene una tarea por ID.
     * @param id Identificador de la tarea.
     * @return La tarea encontrada.
     */
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_MESSAGE + id));
    }

    /**
     * Metodo para listar todas las tareas
     * @param pageRequest
     * @return
     */
    public Page<Task> getAllTasks(PageRequest pageRequest) {
        return taskRepository.findAll(pageRequest);
    }

    /**
     * Método para obtener las tareas de un usuario con paginación
     * @param username
     * @param pageRequest
     * @return
     */
    public Page<Task> getTasksByUser(String username, PageRequest pageRequest) {
        return taskRepository.findByUsername(username, pageRequest);
    }
}