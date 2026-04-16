package com.salah.mcpplayersservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CancellationRequest(@NotBlank(message = "Cancellation reason is required") String reason) {
}
