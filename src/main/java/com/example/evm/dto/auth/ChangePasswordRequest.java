package com.example.evm.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

	@NotBlank(message = "oldPassword is required")
	private String oldPassword;

	@NotBlank(message = "newPassword is required")
	@Size(min = 6, message = "newPassword must be at least 6 characters")
	private String newPassword;
}


