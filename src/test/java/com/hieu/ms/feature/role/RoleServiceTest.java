package com.hieu.ms.feature.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.hieu.ms.feature.role.dto.RoleRequest;
import com.hieu.ms.feature.role.dto.RoleResponse;
import com.hieu.ms.feature.role.dto.RoleSearchRequest;
import com.hieu.ms.shared.dto.response.PageResponse;
import com.querydsl.core.types.Predicate;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    RoleRepository roleRepository;

    @Mock
    PermissionRepository permissionRepository;

    @Mock
    RoleMapper roleMapper;

    @InjectMocks
    RoleService roleService;

    private Role role;
    private RoleResponse roleResponse;

    @BeforeEach
    void setUp() {
        role = Role.builder().name("USER").description("Regular user").build();
        roleResponse =
                RoleResponse.builder().name("USER").description("Regular user").build();
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("maps request, sets permissions, saves and returns response")
        void create_savesAndReturns() {
            RoleRequest request = RoleRequest.builder()
                    .name("USER")
                    .description("Regular user")
                    .permissions(Set.of("READ"))
                    .build();
            Permission perm = Permission.builder().name("READ").build();

            when(roleMapper.toRole(request)).thenReturn(role);
            when(permissionRepository.findAllById(Set.of("READ"))).thenReturn(List.of(perm));
            when(roleRepository.save(role)).thenReturn(role);
            when(roleMapper.toRoleResponse(role)).thenReturn(roleResponse);

            RoleResponse result = roleService.create(request);

            assertThat(result).isEqualTo(roleResponse);
            verify(roleRepository).save(role);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAllTests {

        @Test
        @DisplayName("returns mapped list of all roles")
        void getAll_returnsMappedList() {
            when(roleRepository.findAll()).thenReturn(List.of(role));
            when(roleMapper.toRoleResponse(role)).thenReturn(roleResponse);

            List<RoleResponse> result = roleService.getAll();

            assertThat(result).hasSize(1).containsExactly(roleResponse);
        }
    }

    @Nested
    @DisplayName("getRoles")
    class GetRolesTests {

        @Test
        @DisplayName("null keyword: no predicate filter, returns paginated result")
        void nullKeyword_returnsPaginatedResult() {
            RoleSearchRequest request = new RoleSearchRequest();
            // keyword is null by default

            Page<Role> page = new PageImpl<>(List.of(role));
            when(roleRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(page);
            when(roleMapper.toRoleResponse(role)).thenReturn(roleResponse);

            PageResponse<RoleResponse> result = roleService.getRoles(request);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("non-null keyword: applies predicate, returns paginated result")
        void nonNullKeyword_appliesPredicate() {
            RoleSearchRequest request = new RoleSearchRequest();
            request.setKeyword("USER");

            Page<Role> page = new PageImpl<>(List.of(role));
            when(roleRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(page);
            when(roleMapper.toRoleResponse(role)).thenReturn(roleResponse);

            PageResponse<RoleResponse> result = roleService.getRoles(request);

            assertThat(result.getContent()).hasSize(1);
            verify(roleRepository).findAll(any(Predicate.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("calls deleteById")
        void delete_callsDeleteById() {
            roleService.delete("USER");

            verify(roleRepository).deleteById("USER");
        }
    }
}
