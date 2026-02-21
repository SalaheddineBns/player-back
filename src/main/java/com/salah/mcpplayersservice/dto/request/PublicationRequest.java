package com.salah.mcpplayersservice.dto.request;

import com.salah.mcpplayersservice.models.PublicationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PublicationRequest(@NotBlank(message = "Title cannot be empty") String title,
		@NotBlank(message = "Content cannot be empty") String content,
		@NotNull(message = "Publication type is required") PublicationType publicationType) {
}
