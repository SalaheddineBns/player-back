package com.salah.mcpplayersservice.dto.request;

import com.salah.mcpplayersservice.models.PlayerStatus;

import java.util.UUID;

public record PlayerUpdateRequest(String firstName, String lastName, String gender, String position, String nationality,
		String city, String preferredLeg, Integer preferredNumber, UUID teamId, PlayerStatus status) {
}
