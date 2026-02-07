package com.salah.mcpplayersservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = "Username cannot be empty") String userName,
		@NotBlank(message = "Password cannot be empty") String password) {
}
