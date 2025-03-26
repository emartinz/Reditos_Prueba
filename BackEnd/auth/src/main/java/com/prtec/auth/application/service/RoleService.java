package com.prtec.auth.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prtec.auth.adapter.out.repository.IRoleRepository;
import com.prtec.auth.adapter.out.repository.IUserRoleRepository;
import com.prtec.auth.domain.model.entities.Role;
import com.prtec.auth.domain.model.entities.User;
import com.prtec.auth.domain.model.entities.UserRole;

/**
 * Servicio para gestionar operaciones relacionadas con Roles
 * 
 * @author: Edgar Martinez
 * @version: 1.0
 */
@Service
public class RoleService {
    private final IRoleRepository roleRepository;
    private final IUserRoleRepository userRoleRepository;

    public RoleService(IRoleRepository roleRepository, IUserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Método para consultar roles por su Id
     * 
     * @param userId
     * @return Lista de roles
     */
    public List<Role> getRolesByUserId(Long userId) {
        return roleRepository.findRolesByUserId(userId);
    }

    /**
     * Método para persistir objeto role
     * 
     * @param role
     * @return Rol guardado
     */
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    /**
     * Método para obtener roles por una lista de IDs
     * 
     * @param roleIds
     * @return Lista de roles encontrados
     */
    public List<Role> findByIds(List<Long> roleIds) {
        return roleRepository.findAllById(roleIds);
    }

    /**
     * Método para establecer roles a un usuario
     * 
     * <b>Nota:</b> Este método borra todos los roles asociados y asocia los que se hayan definido en el parámetro userId
     * 
     * @param userId
     * @param roleIds
     */
    @Transactional
    public void setRolesToUser(Long userId, List<Long> roleIds) {
        userRoleRepository.deleteByUserId(userId);
        User user = new User();
        user.setId(userId);

        roleIds.forEach(roleId -> {
            Role role = new Role();
            role.setId(roleId);

            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        });
    }

    /**
     * Método para asignar roles a un usuario específico
     * 
     * @param userId
     * @param roleIds
     */
    @Transactional
    public void assignRolesToUser(Long userId, List<Role> roles) {
        // Obtener los roles ya asignados al usuario
        List<Role> existingRoles = getRolesByUserId(userId);

        // Filtrar los nuevos roles que no estén ya en la lista de roles existentes
        List<Role> rolesToAssign = roles.stream()
                                        .filter(role -> !existingRoles.contains(role))
                                        .toList();

        // Asignar roles nuevos
        rolesToAssign.forEach(role -> userRoleRepository.assignRoleToUser(userId, role.getId()));
    }
}