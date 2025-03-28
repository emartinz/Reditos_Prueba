package com.prtec.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prtec.tasks.application.service.UserDetailsService;
import com.prtec.tasks.adapter.out.repository.IUserDetailsRepository;
import com.prtec.tasks.domain.model.entity.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class UserDetailsServiceTest {

    @Mock
    private IUserDetailsRepository repository;

    @InjectMocks
    private UserDetailsService userDetailsService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userDetails = new UserDetails();
        userDetails.setUserId(1L);
        userDetails.setUsername("testUser");
        userDetails.setEmail("testuser@example.com");
        userDetails.setFirstName("Test");
        userDetails.setLastName("User");
    }

    @Test
    void testSaveOrUpdateUserDetails_whenUserExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(userDetails));
        when(repository.save(any(UserDetails.class))).thenReturn(userDetails);

        UserDetails updatedUser = userDetailsService.saveOrUpdateUserDetails(1L, "testUser", "newemail@example.com", "UpdatedFirst", "UpdatedLast");

        assertNotNull(updatedUser);
        assertEquals("testUser", updatedUser.getUsername());
        assertEquals("newemail@example.com", updatedUser.getEmail());
        assertEquals("UpdatedFirst", updatedUser.getFirstName());
        assertEquals("UpdatedLast", updatedUser.getLastName());
        verify(repository, times(1)).save(userDetails);
    }

    @Test 
    void testSaveOrUpdateUserDetails_whenUserDoesNotExist() {
        // Simulamos que el repositorio no encuentra el usuario con ID 1
        when(repository.findById(1L)).thenReturn(Optional.empty());
        
        // Creamos un UserDetails con los valores esperados para la prueba
        UserDetails nuevoUserDetails = new UserDetails();
        nuevoUserDetails.setUserId(1L);
        nuevoUserDetails.setUsername("nuevoUser");
        nuevoUserDetails.setEmail("nuevoUser@example.com");
        nuevoUserDetails.setFirstName("Nombre");
        nuevoUserDetails.setLastName("Apellido");

        // Simulamos que el repositorio guardará el nuevo usuario.
        when(repository.save(any(UserDetails.class))).thenReturn(nuevoUserDetails);

        // Llamamos al método saveOrUpdateUserDetails con un usuario que no existe.
        UserDetails createdUser = userDetailsService.saveOrUpdateUserDetails(1L, "nuevoUser", "nuevoUser@example.com", "Nombre", "Apellido");

        // Comprobamos que el resultado no sea null y que los valores sean correctos
        assertNotNull(createdUser);
        assertEquals("nuevoUser", createdUser.getUsername());
        assertEquals("nuevoUser@example.com", createdUser.getEmail());
        assertEquals("Nombre", createdUser.getFirstName());
        assertEquals("Apellido", createdUser.getLastName());
        
        // Verificamos que el repositorio haya guardado el nuevo usuario
        verify(repository, times(1)).save(any(UserDetails.class));
    }

    @Test
    void testFindByUsername() {
        when(repository.findByUsername("testUser")).thenReturn(Optional.of(userDetails));

        Optional<UserDetails> foundUser = userDetailsService.findByUsername("testUser");

        assertTrue(foundUser.isPresent());
        assertEquals("testUser", foundUser.get().getUsername());
        verify(repository, times(1)).findByUsername("testUser");
    }

    @Test
    void testFindOrCreateUser_whenUserExists() {
        when(repository.findByUsername("testUser")).thenReturn(Optional.of(userDetails));

        UserDetails result = userDetailsService.findOrCreateUser(1L, "testUser");

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(repository, times(1)).findByUsername("testUser");
        verify(repository, times(0)).save(any(UserDetails.class));
    }

    @Test
    void testFindOrCreateUser_whenUserDoesNotExist() {
        // Simulamos que el repositorio no encuentra el usuario con el nombre de usuario "nuevoUser"
        when(repository.findByUsername("nuevoUser")).thenReturn(Optional.empty());

        // Creamos un nuevo UserDetails con el username esperado "nuevoUser"
        UserDetails nuevoUserDetails = new UserDetails();
        nuevoUserDetails.setUserId(1L);
        nuevoUserDetails.setUsername("nuevoUser");
        nuevoUserDetails.setEmail("nuevoUser@example.com");
        nuevoUserDetails.setFirstName("Nombre");
        nuevoUserDetails.setLastName("Apellido");

        // Simulamos que el repositorio guardará el nuevo usuario
        when(repository.save(any(UserDetails.class))).thenReturn(nuevoUserDetails);

        // Llamamos al método findOrCreateUser
        UserDetails result = userDetailsService.findOrCreateUser(1L, "nuevoUser");

        // Comprobamos que el resultado no sea null y que el username sea el correcto
        assertNotNull(result);
        assertEquals("nuevoUser", result.getUsername());

        // Verificamos que el repositorio haya buscado por el username "nuevoUser" y luego haya guardado el nuevo usuario
        verify(repository, times(1)).findByUsername("nuevoUser");
        verify(repository, times(1)).save(any(UserDetails.class));
    }
}