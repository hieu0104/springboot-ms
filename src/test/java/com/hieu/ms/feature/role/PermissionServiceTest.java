package com.hieu.ms.feature.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hieu.ms.feature.role.dto.PermissionRequest;
import com.hieu.ms.feature.role.dto.PermissionResponse;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    PermissionRepository permissionRepository;

    @Mock
    PermissionMapper permissionMapper;

    @InjectMocks
    PermissionService permissionService;

    @Test
    @DisplayName("create: maps, saves, and returns response")
    void create_mapsAndSaves() {
        PermissionRequest request = PermissionRequest.builder()
                .name("READ")
                .description("Read access")
                .build();
        Permission permission = Permission.builder().name("READ").description("Read access").build();
        Permission saved = Permission.builder().name("READ").description("Read access").build();
        PermissionResponse response = PermissionResponse.builder().name("READ").description("Read access").build();

        when(permissionMapper.toPermission(request)).thenReturn(permission);
        when(permissionRepository.save(permission)).thenReturn(saved);
        when(permissionMapper.toPermissionResponse(saved)).thenReturn(response);

        PermissionResponse result = permissionService.create(request);

        assertThat(result).isEqualTo(response);
        verify(permissionRepository).save(permission);
    }

    @Test
    @DisplayName("getAll: returns mapped list of all permissions")
    void getAll_returnsMappedList() {
        Permission p1 = Permission.builder().name("READ").build();
        Permission p2 = Permission.builder().name("WRITE").build();
        PermissionResponse r1 = PermissionResponse.builder().name("READ").build();
        PermissionResponse r2 = PermissionResponse.builder().name("WRITE").build();

        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));
        when(permissionMapper.toPermissionResponse(p1)).thenReturn(r1);
        when(permissionMapper.toPermissionResponse(p2)).thenReturn(r2);

        List<PermissionResponse> result = permissionService.getAll();

        assertThat(result).hasSize(2).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("delete: calls deleteById")
    void delete_callsDeleteById() {
        permissionService.delete("READ");

        verify(permissionRepository).deleteById("READ");
    }
}
