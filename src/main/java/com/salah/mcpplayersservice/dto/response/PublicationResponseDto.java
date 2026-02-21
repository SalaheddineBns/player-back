package com.salah.mcpplayersservice.dto.response;

import com.salah.mcpplayersservice.models.PublicationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record PublicationResponseDto(UUID publicationId, String title, String content, PublicationType publicationType,
		LocalDateTime createdAt, UUID teamId, String teamName, String teamLogoUrl) {
}
