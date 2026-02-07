package com.salah.mcpplayersservice.dto.response;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

public record TeamResponseDto(UUID teamId, String teamName, Date dateCreated, String coach,
		Set<PlayerResponseDto> players) {
}
