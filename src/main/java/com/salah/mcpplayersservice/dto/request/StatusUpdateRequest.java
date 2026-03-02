package com.salah.mcpplayersservice.dto.request;

import com.salah.mcpplayersservice.models.TrialApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull TrialApplicationStatus status) {
}
