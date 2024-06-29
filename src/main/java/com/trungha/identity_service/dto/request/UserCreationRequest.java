package com.trungha.identity_service.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor // dùng để override cái constructor bên service khi new 1 obj, tức là User user1 = new User
@AllArgsConstructor
public class UserCreationRequest {
    @Size(min = 3, message = "USERNAME_INVALID")
    private String username;
    @Size(min = 8, message = "PASSWORD_ERROR")
    private String password;
    private String firstname;
    private String lastname;
    private LocalDate dob;
}
