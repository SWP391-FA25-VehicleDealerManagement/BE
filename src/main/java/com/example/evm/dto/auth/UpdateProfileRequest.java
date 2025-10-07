package com.example.evm.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {

	@Size(max = 255)
	private String fullName;

	@Size(max = 50)
	private String phone;

	@Email
	@Size(max = 255)
	private String email;
}


