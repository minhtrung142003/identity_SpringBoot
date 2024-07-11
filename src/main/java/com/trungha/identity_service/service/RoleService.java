package com.trungha.identity_service.service;

import com.trungha.identity_service.dto.request.RoleRequest;
import com.trungha.identity_service.dto.response.RoleResponse;
import com.trungha.identity_service.mapper.RoleMapper;
import com.trungha.identity_service.repository.PermissionRepository;
import com.trungha.identity_service.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse createRole(RoleRequest request) {
        var role = roleMapper.toRole(request); // chuyen doi tu RoleRequest sang role
        var permissions = permissionRepository.findAllById(request.getPermissions()); // build permission vao role
        role.setPermissions(new HashSet<>(permissions));
        role = roleRepository.save(role); // luu role vao db
        return roleMapper.toRoleResponse(role); // tra ve toRoleResponse tu role
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    public void delete(String role) {
        roleRepository.deleteById(role);
    }
}
