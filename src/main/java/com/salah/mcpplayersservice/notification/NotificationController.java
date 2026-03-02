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

	// ── Manager endpoints ─────────────────────────────────────────────────────

	@Operation(summary = "Get manager notifications (player applied)")
	@GetMapping
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(notificationService.getNotifications(user));
	}

	@Operation(summary = "Get manager unread notification count")
	@GetMapping("/unread-count")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user)));
	}

	// ── Player endpoints ──────────────────────────────────────────────────────

	@Operation(summary = "Get player notifications (status changed)")
	@GetMapping("/player")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<List<NotificationResponse>> getPlayerNotifications(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(notificationService.getPlayerNotifications(user));
	}

	@Operation(summary = "Get player unread notification count")
	@GetMapping("/player/unread-count")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<Map<String, Long>> getPlayerUnreadCount(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(Map.of("count", notificationService.getPlayerUnreadCount(user)));
	}

	// ── Shared endpoints (mark as read) ───────────────────────────────────────

	@Operation(summary = "Mark a notification as read")
	@PutMapping("/{id}/read")
	public ResponseEntity<Void> markAsRead(@PathVariable String id, @AuthenticationPrincipal User user) {
		notificationService.markAsRead(id, user);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Mark all notifications as read (for the current user)")
	@PutMapping("/read-all")
	public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
		notificationService.markAllAsRead(user);
		return ResponseEntity.noContent().build();
	}

}
