package com.prtec.auth;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.prtec.auth.adapter.out.repository.IUserRepository;
import com.prtec.auth.application.service.UserService;
import com.prtec.auth.domain.model.entities.User;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private IUserRepository userRepository;

    @Test
    void testFindUserById() {
        Long userId = 1L;
        User mockUser = new User(userId, "testUser", "password123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        Optional<User> result = userService.findUserById(userId);
        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
    }

    @Test
    void testSaveUser() {
        User user = new User(1L, "newUser", "password123");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.saveUser(user);
        assertNotNull(result);
        assertEquals("newUser", result.getUsername());
    }
}