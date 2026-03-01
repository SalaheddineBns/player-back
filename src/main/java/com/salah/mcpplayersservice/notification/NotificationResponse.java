package com.salah.mcpplayersservice.notification;

import java.time.LocalDateTime;

public record NotificationResponse(String notificationId, String playerName, String trialLocation,
		LocalDateTime trialDate, String trialId, boolean isRead, LocalDateTime createdAt) {
}
