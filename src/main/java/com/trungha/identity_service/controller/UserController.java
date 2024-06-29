package com.trungha.identity_service.controller;

import com.trungha.identity_service.dto.request.ApiResponse;
import com.trungha.identity_service.dto.request.UserCreationRequest;
import com.trungha.identity_service.dto.request.UserUpdateRequest;
import com.trungha.identity_service.entity.User;
import com.trungha.identity_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController { // tương tác với các class service

    @Autowired
    private UserService userService;

    @PostMapping
    ApiResponse<User> createUser(@RequestBody @Valid UserCreationRequest request) {  //dùng để map data vào obj
        ApiResponse<User> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }
    @GetMapping
    List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    User getUser(@PathVariable("userId") String userId) {
        return userService.getUser(userId);
    }

    @PutMapping("/{userId}")
    User updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        return  userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    String User(@PathVariable String userId) {
        userService.deleteUser(userId);
        return "Delete has been successfull";
    }

}
