package com.salah.mcpplayersservice.dto.response;

import java.util.UUID;

public record TeamOptionResponseDto(UUID teamId, String teamName, String logoUrl) {
}
