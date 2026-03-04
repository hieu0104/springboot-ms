package com.hieu.ms.feature.role;

import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.hieu.ms.feature.role.dto.RoleRequest;
import com.hieu.ms.feature.role.dto.RoleResponse;
import com.hieu.ms.feature.role.dto.RoleSearchRequest;
import com.hieu.ms.shared.dto.response.PageResponse;
import com.querydsl.core.BooleanBuilder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);

        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    /**
     * Get roles with pagination and search using QueryDSL
     *
     * @param request Search criteria and pagination info
     * @return PageResponse containing roles
     */
    public PageResponse<RoleResponse> getRoles(RoleSearchRequest request) {
        // Build predicate
        QRole qRole = QRole.role;
        BooleanBuilder predicate = new BooleanBuilder();

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = request.getKeyword().trim();
            predicate.and(qRole.name.containsIgnoreCase(keyword).or(qRole.description.containsIgnoreCase(keyword)));
        }

        // Get pageable from request
        Pageable pageable = request.getPageable(Sort.by("name").ascending());

        // Execute query
        Page<RoleResponse> rolePage =
                roleRepository.findAll(predicate, pageable).map(roleMapper::toRoleResponse);

        return PageResponse.of(rolePage);
    }

    public void delete(String role) {
        roleRepository.deleteById(role);
    }

    public java.util.Optional<Role> findRoleById(String roleId) {
        return roleRepository.findById(roleId);
    }

    public List<Role> findRolesByIds(List<String> roleIds) {
        return roleIds == null ? List.of() : roleRepository.findAllById(roleIds);
    }
}
