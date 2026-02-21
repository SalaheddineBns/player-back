package com.salah.mcpplayersservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MediaResponseDto(UUID mediaId, String title, String description, String mediaType, String fileName,
		String contentType, Long fileSize, LocalDateTime uploadDate, long viewCount, String mediaUrl) {
}
