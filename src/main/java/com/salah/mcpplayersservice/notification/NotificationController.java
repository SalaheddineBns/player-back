package com.salah.mcpplayersservice.notification;

import com.salah.mcpplayersservice.models.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "In-app notification endpoints")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Operation(summary = "Get notifications", description = "Returns notifications for the current team manager")
	@GetMapping
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(notificationService.getNotifications(user));
	}

	@Operation(summary = "Get unread notification count")
	@GetMapping("/unread-count")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User user) {
		long count = notificationService.getUnreadCount(user);
		return ResponseEntity.ok(Map.of("count", count));
	}

	@Operation(summary = "Mark a notification as read")
	@PutMapping("/{id}/read")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<Void> markAsRead(@PathVariable String id, @AuthenticationPrincipal User user) {
		notificationService.markAsRead(id, user);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Mark all notifications as read")
	@PutMapping("/read-all")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
		notificationService.markAllAsRead(user);
		return ResponseEntity.noContent().build();
	}

}
