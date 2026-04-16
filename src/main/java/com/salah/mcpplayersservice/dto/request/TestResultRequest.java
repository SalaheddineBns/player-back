package com.salah.mcpplayersservice.dto.request;

import com.salah.mcpplayersservice.models.TestResultStatus;
import jakarta.validation.constraints.NotNull;

public record TestResultRequest(@NotNull(message = "Result status is required") TestResultStatus status, String notes) {
}
