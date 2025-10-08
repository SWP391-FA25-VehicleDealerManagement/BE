package com.example.evm.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProfileRequest {

	@Size(max = 255)
	@JsonAlias({"full_name"})
	private String fullName;

	@Size(max = 50)
	@JsonAlias({"phone_number"})
	private String phone;

	@Email
	@Size(max = 255)
	private String email;
}


