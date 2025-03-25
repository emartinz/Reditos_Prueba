package com.prtec.auth.domain.model.entities;

import org.springframework.data.annotation.Id;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="role")
public class Role {
    @Id
    private Long id;
    private String name;
}
