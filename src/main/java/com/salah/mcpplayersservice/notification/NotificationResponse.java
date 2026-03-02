package com.salah.mcpplayersservice.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record NotificationResponse(String notificationId, String playerName, String trialLocation,
		LocalDateTime trialDate, String trialId, @JsonProperty("isRead") boolean isRead, LocalDateTime createdAt,
		String notificationType, String newStatus, String managerMessage) {
}
