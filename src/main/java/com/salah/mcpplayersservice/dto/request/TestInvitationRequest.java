package com.salah.mcpplayersservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestInvitationRequest(@NotNull(message = "Player user ID is required") UUID playerUserId,
		@NotNull(message = "Proposed date is required") @Future(
				message = "Proposed date must be in the future") LocalDateTime proposedDate,
		String message) {
}
