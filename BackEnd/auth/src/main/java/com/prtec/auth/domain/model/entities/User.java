package com.prtec.auth.domain.model.entities;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="user")
public class User {
    @Id
    private Long id;
    private String username;
    private String password;

    @Transient
    private List<Role> roles = new ArrayList<>();

    @Transient
    @JsonIgnore
    private List<Long> roleIds = new ArrayList<>();
}