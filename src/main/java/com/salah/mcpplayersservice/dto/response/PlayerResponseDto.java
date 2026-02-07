package com.salah.mcpplayersservice.dto.response;

import java.util.UUID;

public record PlayerResponseDto(UUID playerId, String firstName, String lastName, String userName, String email,
		String position, String nationality) {
}
