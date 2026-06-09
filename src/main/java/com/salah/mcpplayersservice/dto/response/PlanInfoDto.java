package com.salah.mcpplayersservice.dto.response;

import com.salah.mcpplayersservice.models.SubscriptionPlan;

import java.util.List;

public record PlanInfoDto(SubscriptionPlan plan, String displayName, double pricePerMonth, List<String> features,
		int maxVideos, int maxTrialApplicationsPerMonth, boolean canContactManager, boolean unlimitedVideos,
		boolean unlimitedTrials, boolean popular) {

}
