package com.prtec.auth.domain.model.entities;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prtec.auth.application.service.RoleService;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;

    @Transient
    private List<Role> roles = new ArrayList<>();

    @Transient
    @JsonIgnore
    private List<Long> roleIds = new ArrayList<>();

    public User(Long userid, String username, String password) {
        this.id = userid;
        this.username = username;
        this.password = password;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Método para obtener los roles asociados al usuario
     * 
     * @param roleService Servicio para obtener roles
     * @return Lista de roles
     */
    public List<Role> getRoles(RoleService roleService) {
        if (!roles.isEmpty()) {
            return roles;
        }
        roles = roleService.getRolesByUserId(this.id);
        return roles;
    }

    /**
     * Método para asignar una lista de roles al usuario
     * 
     * @param roles Lista de roles
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    /**
     * Método para asignar roles usando sus IDs
     * 
     * @param roleIds Lista de IDs de roles
     * @param roleService Servicio para obtener roles
     */
    public void setRoleIds(List<Long> roleIds, RoleService roleService) {
        this.roles = roleService.findByIds(roleIds);
    }

    /**
     * Método para cargar roles desde el RoleService
     * 
     * @param roleService Servicio para obtener roles
     */
    public void loadRoles(RoleService roleService) {
        this.roles = roleService.getRolesByUserId(this.id);
    }
}