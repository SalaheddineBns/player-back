package com.salah.mcpplayersservice.dto.response;

import com.salah.mcpplayersservice.models.TestSlotStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestSlotResponseDto(UUID slotId, UUID invitationId, LocalDateTime scheduledDate, TestSlotStatus status,
		String cancellationReason, LocalDateTime cancelledAt, LocalDateTime createdAt, TestResultResponseDto result) {
}
