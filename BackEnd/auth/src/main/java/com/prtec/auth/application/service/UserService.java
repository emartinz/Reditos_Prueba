package com.prtec.auth.application.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.prtec.auth.adapter.out.repository.IUserRepository;
import com.prtec.auth.domain.model.entities.User;

import jakarta.transaction.Transactional;

import java.util.Optional;

/**
 * Servicio que gestiona las operaciones relacionadas con Usuarios
 * 
 * @author: Edgar Martinez
 * @version: 1.0
 */
@Service
@Transactional
public class UserService {
    private final IUserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }
} 
