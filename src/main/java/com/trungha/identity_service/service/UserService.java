package com.trungha.identity_service.service;

import com.trungha.identity_service.dto.request.UserCreationRequest;
import com.trungha.identity_service.dto.request.UserUpdateRequest;
import com.trungha.identity_service.dto.response.UserResponse;
import com.trungha.identity_service.entity.User;
import com.trungha.identity_service.enums.Role;
import com.trungha.identity_service.exception.AppException;
import com.trungha.identity_service.exception.ErrorCode;
import com.trungha.identity_service.mapper.UserMapper;
import com.trungha.identity_service.repository.RoleRepository;
import com.trungha.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor // tu dong create constructor va inject dependency nay vao
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // make true tuc la ko khai bao se tu dong thanh private final
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    public User createUser(UserCreationRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request); // dùng mapper để gọi ra all
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // set roles
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(Role.USER.name());
        //user.setRoles(hashSet);

        return userRepository.save(user);
    }

    // chi co admin get all user
    @PreAuthorize("authentication.name == 'admin'")
    public List<UserResponse> getUsers() {
        log.info("In method get all user"); 
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    // user chi dc get thong tin cua user do
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String id){
        log.info("In method get user id");
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    // Cách lấy thông tin đăng nhập là ai
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found"));
        userMapper.updateUser(user, request); // dung mapper de goi all

        user.setPassword(passwordEncoder.encode(request.getPassword())); // update password
        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles)); // map role vao user
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}
