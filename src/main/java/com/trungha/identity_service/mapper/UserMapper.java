package com.trungha.identity_service.mapper;

import com.trungha.identity_service.dto.request.UserCreationRequest;
import com.trungha.identity_service.dto.request.UserUpdateRequest;
import com.trungha.identity_service.dto.response.UserResponse;
import com.trungha.identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring") // dung de dependency inject cai mapper vao spring
public interface UserMapper {
    User toUser(UserCreationRequest request);
    void updateUser(@MappingTarget User user, UserUpdateRequest request); // dung de define map thg UserUpdateRequest vào User
    UserResponse toUserResponse(User user); // trả về data mong muốn trong dto
}
