package com.prtec.auth.adapter.out.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.prtec.auth.domain.model.entities.Role;
import com.prtec.auth.domain.model.entities.User;

import java.util.List;
import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT r FROM Role r JOIN UserRole ur ON ur.role.id = r.id WHERE ur.user.id = :userId")
    List<Role> findRolesByUserId(Long userId);
} 
