package com.salah.mcpplayersservice.dto.response;

import com.salah.mcpplayersservice.models.TestInvitationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestInvitationResponseDto(UUID invitationId, UUID managerId, String managerTeamName,
		String managerTeamLogo, UUID playerUserId, String playerFirstName, String playerLastName,
		LocalDateTime proposedDate, String message, TestInvitationStatus status, LocalDateTime createdAt,
		LocalDateTime respondedAt,
		/** Null until invitation is accepted */
		TestSlotResponseDto slot) {
}
