package com.trungha.identity_service.dto.request;

import com.trungha.identity_service.validator.DobConstraint;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor // dùng để override cái constructor bên service khi new 1 obj, tức là User user1 = new User
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 3, message = "USERNAME_INVALID")
    String username;
    @Size(min = 8, message = "PASSWORD_ERROR")
    String password;
    String firstname;
    String lastname;
    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;
}
