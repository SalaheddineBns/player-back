package com.salah.mcpplayersservice.dto.response;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record TeamPageResponseDto(UUID teamId, String teamName, String description, String division, String coach,
		Date dateCreated, String logoUrl, int playerCount, List<PublicationResponseDto> publications,
		List<RecruitmentNeedResponseDto> recruitmentNeeds) {
}
