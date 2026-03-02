package com.salah.mcpplayersservice.dto.response;

import com.salah.mcpplayersservice.models.TrialApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerApplicationSummaryDto(UUID trialId, UUID candidateId, TrialApplicationStatus status,
		LocalDateTime appliedAt, LocalDateTime statusUpdatedAt) {
}
