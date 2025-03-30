package com.prtec.auth;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.prtec.auth.application.service.UserService;
import com.prtec.auth.domain.model.dto.ApiResponseDTO;
import com.prtec.auth.domain.model.entities.User;
import com.prtec.auth.adapter.in.controller.UserController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@SuppressWarnings("null")
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserSuccess() {
        User mockUser = new User();
        mockUser.setUsername("testuser");

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        ResponseEntity<ApiResponseDTO<User>> response = userController.getUser("testuser");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SUCCESS", response.getBody().getStatus().toString());
        assertEquals("testuser", response.getBody().getData().getUsername());
    }

    @Test
    void testGetUserNotFound() {
        when(userService.findByUsername("unknownuser")).thenReturn(Optional.empty());

        ResponseEntity<ApiResponseDTO<User>> response = userController.getUser("unknownuser");

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("ERROR", response.getBody().getStatus().toString());
        assertEquals("Usuario no encontrado", response.getBody().getMessage());
    }
}