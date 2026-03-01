package com.salah.mcpplayersservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TrialResponseDto(UUID trialId, String location, LocalDateTime trialDate, String position,
		String description, LocalDateTime createdAt, UUID teamId, String teamName, String teamLogoUrl,
		int candidateCount) {
}
