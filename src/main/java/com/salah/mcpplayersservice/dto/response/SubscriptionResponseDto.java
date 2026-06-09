package com.salah.mcpplayersservice.dto.response;

import com.salah.mcpplayersservice.models.SubscriptionPlan;
import com.salah.mcpplayersservice.models.SubscriptionStatus;

import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionResponseDto(UUID id, SubscriptionPlan plan, SubscriptionStatus status, LocalDate startDate,
		LocalDate endDate, double pricePerMonth, int maxVideos, int maxTrialApplicationsPerMonth,
		boolean canContactManager, boolean unlimitedVideos, boolean unlimitedTrials) {

}
