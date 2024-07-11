package com.trungha.identity_service.service;

import com.trungha.identity_service.dto.request.PermissionRequest;
import com.trungha.identity_service.dto.response.PermissionResponse;
import com.trungha.identity_service.entity.Permission;
import com.trungha.identity_service.mapper.PermissionMapper;
import com.trungha.identity_service.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {

    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse createPermission(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request); // chuyen doi tu PermissionRequest sang permission
        permission = permissionRepository.save(permission); // luu permission vao DB
        return permissionMapper.toPermissionResponse(permission); // chuyển doi sang toPermissionResponse va tra ve kq
    }

    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void deletePermission(String permission) {
        permissionRepository.deleteById(permission);
    }
}
