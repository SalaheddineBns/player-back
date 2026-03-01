package com.salah.mcpplayersservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriberResponseDto(UUID playerId, String firstName, String lastName, String position,
		String nationality, String city, String profilePictureUrl, String status, LocalDateTime subscribedAt) {
}
