package com.salah.mcpplayersservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TrialRequest(@NotBlank(message = "Location is required") String location,
		@NotNull(message = "Trial date is required") LocalDateTime trialDate, String position, String description) {
}
