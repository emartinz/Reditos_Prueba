package com.prtec.tasks.domain.model.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name="user_details")
public class UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(nullable = false, unique = true)
    @JsonIgnore
    private Long userId; // Este Id lo toma del servicio auth al autenticarse exitosamente

    @Column(nullable = false, unique = true)
    private String username;

    @Column
    private String email;

    @Column(nullable = true, length = 100, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_general_ci")
    private String firstName;

    @Column(nullable = true, length = 100, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_general_ci")
    private String lastName;

    @OneToMany(mappedBy = "userDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Task> tasks;
}