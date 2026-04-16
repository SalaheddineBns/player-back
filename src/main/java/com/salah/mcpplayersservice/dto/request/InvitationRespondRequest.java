package com.salah.mcpplayersservice.dto.request;

import jakarta.validation.constraints.NotNull;

public record InvitationRespondRequest(@NotNull(message = "accepted field is required") Boolean accepted) {
}
