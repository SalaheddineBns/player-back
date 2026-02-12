package com.salah.mcpplayersservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(@NotBlank(message = "First name cannot be empty") String firstName,
		@NotBlank(message = "Last name cannot be empty") String lastName,
		@NotBlank(message = "Username cannot be empty") String userName,
		@NotBlank(message = "Email cannot be empty") @Email(message = "Email must be valid") String email,
		@NotBlank(message = "Password cannot be empty") @Size(min = 6,
				message = "Password must be at least 6 characters") String password,
		String gender) {
}
