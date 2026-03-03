package com.salah.mcpplayersservice.dto.response;

import com.salah.mcpplayersservice.models.TrialApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TrialCandidateResponseDto(UUID candidateId, UUID playerId, String playerFirstName, String playerLastName,
		String playerPosition, String playerLevel, TrialApplicationStatus status, LocalDateTime appliedAt,
		LocalDateTime statusUpdatedAt, String notes) {
}
