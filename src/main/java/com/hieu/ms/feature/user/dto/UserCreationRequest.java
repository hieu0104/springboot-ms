package com.hieu.ms.feature.user.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

import com.hieu.ms.shared.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 4, message = "USERNAME_INVALID")
    String username;

    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;

    String firstName;
    String lastName;

    @jakarta.validation.constraints.Email(message = "INVALID_EMAIL")
    @jakarta.validation.constraints.NotBlank(message = "EMAIL_REQUIRED")
    String email;

    @DobConstraint(min = 10, message = "INVALID_DOB")
    LocalDate dob;

    String city;
}
