package com.salah.mcpplayersservice.dto.request;

import java.util.UUID;

public record PlayerUpdateRequest(String firstName, String lastName, String gender, String position, String nationality,
		String preferredLeg, Integer preferredNumber, UUID teamId) {
}
