package com.prtec.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.prtec.auth.adapter.out.repository.IRoleRepository;
import com.prtec.auth.adapter.out.repository.IUserRoleRepository;
import com.prtec.auth.application.service.RoleService;
import com.prtec.auth.domain.model.entities.Role;
import com.prtec.auth.domain.model.entities.UserRole;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @InjectMocks
    private RoleService roleService;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private IUserRoleRepository userRoleRepository;

    @Test
    void testGetRolesByUserId() {
        Long userId = 1L;
        List<Role> mockRoles = Arrays.asList(
            new Role(1L, "ROLE_USER"),
            new Role(2L, "ROLE_ADMIN")
        );

        when(roleRepository.findRolesByUserId(userId)).thenReturn(mockRoles);

        List<Role> result = roleService.getRolesByUserId(userId);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ROLE_USER", result.get(0).getName());
        assertEquals("ROLE_ADMIN", result.get(1).getName());
    }

    @Test
    void testSaveRole() {
        Role role = new Role(1L, "ROLE_USER");
        when(roleRepository.save(role)).thenReturn(role);

        Role result = roleService.saveRole(role);
        assertNotNull(result);
        assertEquals("ROLE_USER", result.getName());
    }

    @Test
    void testSetRolesToUser() {
        Long userId = 1L;
        List<Long> roleIds = Arrays.asList(1L, 2L);
        
        when(userRoleRepository.deleteByUserId(userId)).thenReturn(1);
        when(userRoleRepository.save(any(UserRole.class)))
            .thenReturn(new UserRole(userId, 1L))
            .thenReturn(new UserRole(userId, 2L));
        
        assertDoesNotThrow(() -> roleService.setRolesToUser(userId, roleIds));

        verify(userRoleRepository, times(1)).deleteByUserId(userId);
        verify(userRoleRepository, times(2)).save(any(UserRole.class));
    }
}