package com.salah.mcpplayersservice.dto.request;

import com.salah.mcpplayersservice.models.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(@NotNull SubscriptionPlan plan) {

}
