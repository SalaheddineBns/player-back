package com.salah.mcpplayersservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecruitmentNeedResponseDto(UUID id, String position, String description, LocalDateTime createdAt) {
}
