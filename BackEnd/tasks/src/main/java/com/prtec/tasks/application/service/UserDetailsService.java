package com.prtec.tasks.application.service;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.prtec.tasks.adapter.out.repository.IUserDetailsRepository;
import com.prtec.tasks.domain.model.entity.UserDetails;

@Service
@RequiredArgsConstructor
public class UserDetailsService {

    private final IUserDetailsRepository repository;

    public UserDetails saveOrUpdateUserDetails(Long userId, String username, String email, String firstName, String lastName) {
        UserDetails userDetails = repository.findById(userId).orElse(new UserDetails());
        userDetails.setUserId(userId);
        userDetails.setUsername(username);
        userDetails.setEmail(email);
        userDetails.setFirstName(firstName);
        userDetails.setLastName(lastName);

        return repository.save(userDetails);
    }

    public Optional<UserDetails> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public UserDetails findOrCreateUser(Long userId, String username) {
        return repository.findByUsername(username)
            .orElseGet(() -> {
                // Crear nuevo UserDetails asociado a id del auth
                UserDetails newUser = new UserDetails();
                newUser.setUserId(userId);
                newUser.setUsername(username);
                return repository.save(newUser);
            });
    }
}