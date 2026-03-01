package com.salah.mcpplayersservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TeamUpdateRequest(String teamName, String description, String division,
		@NotBlank(message = "City cannot be empty") String city,
		@NotBlank(message = "Country cannot be empty") String country) {
}
