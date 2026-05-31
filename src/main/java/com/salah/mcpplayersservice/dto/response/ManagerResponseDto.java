package com.salah.mcpplayersservice.dto.response;

import java.util.UUID;

public record ManagerResponseDto(UUID managerId, String firstName, String lastName, String profilePictureUrl,
		String userName, String email, PlayerTeamResponseDto team) {
}
