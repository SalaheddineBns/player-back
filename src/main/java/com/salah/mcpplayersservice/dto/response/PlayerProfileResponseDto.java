package com.salah.mcpplayersservice.dto.response;

import java.util.List;
import java.util.UUID;

public record PlayerProfileResponseDto(UUID playerId, String firstName, String lastName, String userName, String email,
		String position, String nationality, String city, String gender, String preferredLeg, Integer preferredNumber,
		String profilePictureUrl, String status, PlayerTeamResponseDto team, List<MediaResponseDto> mediaItems) {
}
