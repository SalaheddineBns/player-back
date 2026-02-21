package com.salah.mcpplayersservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RecruitmentNeedRequest(@NotBlank(message = "Position cannot be empty") String position,
		String description) {
}
