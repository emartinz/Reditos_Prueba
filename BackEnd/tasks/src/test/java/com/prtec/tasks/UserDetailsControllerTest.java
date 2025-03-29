package com.prtec.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prtec.tasks.adapter.in.controller.UserDetailsController;
import com.prtec.tasks.application.service.UserDetailsService;
import com.prtec.tasks.domain.model.dto.UserDetailsRequestDTO;
import com.prtec.tasks.domain.model.entity.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserDetailsControllerTest {
    private static final String STR_VALID_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJBRE1JTiIsIlVTRVIiXSwidXNlcklkIjoxLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc0MzE3NTYzMSwiZXhwIjoxNzc0NzExNjMxfQ.pCsYxqdqrL8AUu5_4ryahzTpr1jZXRRiRmP8IPsRPzI";

    private MockMvc mockMvc;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private UserDetailsController userDetailsController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userDetailsController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSaveOrUpdateUserDetails_Success() throws Exception {
        String authHeader = STR_VALID_TOKEN;

        UserDetailsRequestDTO requestDTO = new UserDetailsRequestDTO();
        requestDTO.setUserId(1L);
        requestDTO.setUsername("testuser");
        requestDTO.setEmail("test@example.com");
        requestDTO.setFirstName("paco");
        requestDTO.setLastName("tilla");

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(1L);
        userDetails.setUsername("testuser");
        userDetails.setEmail("test@example.com");
        userDetails.setFirstName("paco");
        userDetails.setLastName("tilla");

        when(userDetailsService.saveOrUpdateUserDetails(any(), any(), any(), any(), any()))
                .thenReturn(userDetails);

        mockMvc.perform(
                post("/api/user/registerUser", 1L)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("Usuario registrado/actualizado exitosamente"));
    }

    @Test
    void testSaveOrUpdateUserDetails_BadRequest() throws Exception {
        String authHeader = STR_VALID_TOKEN;
        UserDetailsRequestDTO requestDTO = new UserDetailsRequestDTO();
        requestDTO.setUserId(1L);
        requestDTO.setUsername(""); // Simula dato inválido
        requestDTO.setFirstName("paco");
        requestDTO.setLastName("tilla");

        when(userDetailsService.saveOrUpdateUserDetails(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Datos inválidos"));

        mockMvc.perform(post("/api/user/registerUser")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testSaveOrUpdateUserDetails_InternalServerError() throws Exception {
        String authHeader = STR_VALID_TOKEN;
        UserDetailsRequestDTO requestDTO = new UserDetailsRequestDTO();
        requestDTO.setUserId(1L);
        requestDTO.setUsername("testuser");
        requestDTO.setFirstName("paco");
        requestDTO.setLastName("tilla");

        when(userDetailsService.saveOrUpdateUserDetails(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(post("/api/user/registerUser")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))
            )
            .andExpect(status().isInternalServerError());
    }
}