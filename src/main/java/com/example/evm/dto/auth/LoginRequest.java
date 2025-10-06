package com.example.evm.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
<<<<<<< HEAD
import jakarta.validation.constraints.NotBlank;
=======
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
<<<<<<< HEAD
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "password is required")
=======
    private String username;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    private String password;

}
