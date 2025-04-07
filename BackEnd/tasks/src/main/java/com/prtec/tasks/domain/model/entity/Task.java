package com.prtec.tasks.domain.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_general_ci")
	private String title;

	@Column(length = 500)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TaskStatus status = TaskStatus.PENDIENTE;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TaskPriority priority = TaskPriority.MEDIA;

	@Column
	private LocalDate dueDate;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private UserDetails userDetails;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	public enum TaskStatus {
		PENDIENTE,
		EN_PROGRESO,
		COMPLETADA
	}

	public enum TaskPriority {
		ALTA,
		MEDIA,
		BAJA
	}

	public Task(String title, String description, TaskStatus status, TaskPriority priority, LocalDate dueDate,
			UserDetails userDetails) {
		this.title = title;
		this.description = description;
		this.status = status;
		this.priority = priority;
		this.dueDate = dueDate;
		this.userDetails = userDetails;
	}

}
