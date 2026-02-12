package com.salah.mcpplayersservice.dto.request;

public record PlayerUpdateRequest(String firstName, String lastName, String gender, String position,
		String nationality) {
}
