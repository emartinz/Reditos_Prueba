package com.prtec.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.prtec.tasks.adapter.out.repository.ITaskRepository;
import com.prtec.tasks.application.exceptions.TaskNotFoundException;
import com.prtec.tasks.application.service.TaskService;
import com.prtec.tasks.domain.model.entity.Task;
import com.prtec.tasks.domain.model.entity.UserDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    @Mock
    private ITaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("This is a test task");
        task.setStatus(Task.TaskStatus.PENDIENTE);
        task.setPriority(Task.TaskPriority.ALTA);
        UserDetails userDetails = new UserDetails();
        userDetails.setUsername("user1");
        task.setUserDetails(userDetails);
    }

    @Test
    void testCreateTask() {
        // Arrange
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task createdTask = taskService.createTask(task);

        // Assert
        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testCreateTaskWithoutUserDetails() {
        // Arrange
        task.setUserDetails(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(task));
    }

    @Test
    void testUpdateTask() {
        // Arrange
        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated description");
        updatedTask.setStatus(Task.TaskStatus.COMPLETADA);
        updatedTask.setPriority(Task.TaskPriority.MEDIA);
        UserDetails userDetails = new UserDetails();
        userDetails.setUsername("user1");
        updatedTask.setUserDetails(userDetails);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // Act
        Task result = taskService.updateTask(1L, updatedTask);

        // Assert
        assertEquals("Updated Task", result.getTitle());
        assertEquals("Updated description", result.getDescription());
        assertEquals(Task.TaskStatus.COMPLETADA, result.getStatus());
        assertEquals(Task.TaskPriority.MEDIA, result.getPriority());
    }

    @Test
    void testUpdateTaskNotFound() {
        // Arrange
        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Task");

        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(1L, updatedTask));
    }

    @Test
    void testDeleteTask() {
        // Arrange
        when(taskRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = taskService.deleteTask(1L);

        // Assert
        assertTrue(result);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTaskNotFound() {
        // Arrange
        when(taskRepository.existsById(1L)).thenReturn(false);

        // Act
        boolean result = taskService.deleteTask(1L);

        // Assert
        assertFalse(result);
        verify(taskRepository, times(0)).deleteById(1L);
    }

    @Test
    void testGetTaskById() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act
        Task result = taskService.getTaskById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetTaskByIdNotFound() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    void testGetTasksByFilters() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        @SuppressWarnings("unchecked")
        Page<Task> tasksPage = mock(Page.class);
        when(taskRepository.findByUserAndFilters(anyLong(), anyString(), any(), any(), eq(pageRequest)))
                .thenReturn(tasksPage);

        // Act
        Page<Task> result = taskService.getTasksByFilters(1L, "Test", Task.TaskStatus.PENDIENTE.toString(), Task.TaskPriority.ALTA.toString(), pageRequest);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1))
            .findByUserAndFilters(1L, "Test", Task.TaskStatus.PENDIENTE, Task.TaskPriority.ALTA, pageRequest);
    }

    @Test
    void testGetAllTasks() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        @SuppressWarnings("unchecked")
        Page<Task> tasksPage = mock(Page.class);
        when(taskRepository.findAll(pageRequest)).thenReturn(tasksPage);

        // Act
        Page<Task> result = taskService.getAllTasks(pageRequest);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findAll(pageRequest);
    }
}
