package com.prtec.auth.adapter.out.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.prtec.auth.domain.model.entities.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r JOIN UserRole ur ON ur.role.id = r.id WHERE ur.user.id = :userId")
    List<Role> findRolesByUserId(Long userId);

    Optional<Role> findByName(String roleName);
    
    @Query(value = "INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId)", nativeQuery = true)
    void saveUserRole(Long userId, Long roleId);
}
