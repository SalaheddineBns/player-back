package com.salah.mcpplayersservice.dto.response;

import java.util.UUID;

public record PlayerTeamResponseDto(UUID teamId, String teamName, String logoUrl) {
}
