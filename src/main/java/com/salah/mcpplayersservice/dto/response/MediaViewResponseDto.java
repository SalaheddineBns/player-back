package com.salah.mcpplayersservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MediaViewResponseDto(UUID viewId, UUID teamId, String teamName, LocalDateTime viewedAt) {
}
