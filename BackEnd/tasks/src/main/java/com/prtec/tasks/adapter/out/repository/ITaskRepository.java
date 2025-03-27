package com.prtec.tasks.adapter.out.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prtec.tasks.domain.model.entity.Task;

import java.util.List;

@Repository
public interface ITaskRepository extends JpaRepository<Task, Long> {

    // Usuarios solo pueden ver sus propias tareas
    @Query("SELECT t FROM Task t WHERE " +
        "(t.userDetails.id = :userId) AND " + 
        "(:status IS NULL OR t.status = :status) AND " +  
        "(:priority IS NULL OR t.priority = :priority) AND " + 
        "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))") 
    List<Task> findByUserAndFilters(
        @Param("userId") Long userId,
        @Param("title") String title,
        @Param("status") Task.TaskStatus status, 
        @Param("priority") Task.TaskPriority priority
    );

    // El admin puede ver todas las tareas
    @Query("SELECT t FROM Task t WHERE " +
        "(:status IS NULL OR t.status = :status) AND " +
        "(:priority IS NULL OR t.priority = :priority) AND " + 
        "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))") 
    List<Task> findByFilters(
        @Param("title") String title,
        @Param("status") Task.TaskStatus status, 
        @Param("priority") Task.TaskPriority priority
    );
    
}