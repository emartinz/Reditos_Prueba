package com.prtec.auth.application.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.prtec.auth.adapter.out.repository.IUserRepository;
import com.prtec.auth.domain.model.entities.User;

import java.util.Optional;

/**
 * Servicio que gestiona las operaciones relacionadas con Usuarios
 * 
 * @author: Edgar Martinez
 * @version: 1.0
 */
@Service
public class UserService {
    private final IUserRepository userRepository;
    private final RoleService roleService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(IUserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findUserWithRoles(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setRoles(roleService.getRolesByUserId(user.getId()));
                    return user;
                });
    }

    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }
} 
