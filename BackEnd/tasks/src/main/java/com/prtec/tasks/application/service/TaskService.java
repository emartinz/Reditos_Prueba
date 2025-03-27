package com.prtec.tasks.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prtec.tasks.adapter.out.repository.ITaskRepository;
import com.prtec.tasks.application.exceptions.TaskNotFoundException;
import com.prtec.tasks.domain.model.entity.Task;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    public static final String TASK_NOT_FOUND_MESSAGE = "Task not found with id: ";
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
     */
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(TASK_NOT_FOUND_MESSAGE + id);
        }
        taskRepository.deleteById(id);
    }

    /**
     * Obtiene una lista de tareas filtradas por estado y/o prioridad.
     * @param userId
     * @param title
     * @param status
     * @param priority
     * @return
     */
    public List<Task> getTasksByFilters(Long userId, String title, String status, String priority) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio.");
        }

        Task.TaskStatus taskStatus = null;
        Task.TaskPriority taskPriority = null;

        if (status != null) {
            try {
                taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("El estado proporcionado no es válido.");
            }
        }

        if (priority != null) {
            try {
                taskPriority = Task.TaskPriority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("La prioridad proporcionada no es válida.");
            }
        }

        return Optional.ofNullable(taskRepository.findByUserAndFilters(userId, title, taskStatus, taskPriority))
                .orElse(Collections.emptyList());
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
     * @return
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
}