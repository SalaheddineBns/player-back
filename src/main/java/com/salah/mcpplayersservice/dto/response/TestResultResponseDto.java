package com.salah.mcpplayersservice.dto.response;

import com.salah.mcpplayersservice.models.TestResultStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TestResultResponseDto(UUID resultId, TestResultStatus status, String notes, LocalDateTime publishedAt,
		/** Non-null and populated only when status == ACCEPTED */
		List<RequiredDocumentResponseDto> requiredDocuments) {
}
