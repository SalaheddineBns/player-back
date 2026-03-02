package com.salah.mcpplayersservice.notification;

import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final NotificationRepository notificationRepository;

	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	/** Called when a player applies for a trial — notifies the team manager */
	@Transactional
	public void createNotification(User recipient, String playerName, String trialLocation, LocalDateTime trialDate,
			String trialId) {
		Notification notification = Notification.builder()
			.recipient(recipient)
			.playerName(playerName)
			.trialLocation(trialLocation)
			.trialDate(trialDate)
			.trialId(trialId)
			.notificationType(NotificationType.PLAYER_APPLIED)
			.build();
		notificationRepository.save(notification);
	}

	/** Called when the manager changes a candidate's status — notifies the player */
	@Transactional
	public void createStatusChangeNotification(User recipient, String trialLocation, LocalDateTime trialDate,
			String trialId, String newStatus) {
		Notification notification = Notification.builder()
			.recipient(recipient)
			.trialLocation(trialLocation)
			.trialDate(trialDate)
			.trialId(trialId)
			.notificationType(NotificationType.STATUS_CHANGED)
			.newStatus(newStatus)
			.build();
		notificationRepository.save(notification);
	}

	// ── Manager ──────────────────────────────────────────────────────────────

	public List<NotificationResponse> getNotifications(User user) {
		List<Notification> found = notificationRepository.findManagerNotifications(user.getUserId());
		log.info("[getNotifications] userId={} found {} manager notifications", user.getUserId(), found.size());
		return found.stream().map(this::toResponse).toList();
	}

	public long getUnreadCount(User user) {
		long count = notificationRepository.countUnreadManagerNotifications(user.getUserId());
		log.info("[getUnreadCount] userId={} unreadCount={}", user.getUserId(), count);
		return count;
	}

	// ── Player ───────────────────────────────────────────────────────────────

	public List<NotificationResponse> getPlayerNotifications(User user) {
		List<Notification> found = notificationRepository.findPlayerNotifications(user.getUserId());
		log.info("[getPlayerNotifications] userId={} found {} player notifications", user.getUserId(), found.size());
		return found.stream().map(this::toResponse).toList();
	}

	public long getPlayerUnreadCount(User user) {
		long count = notificationRepository.countUnreadPlayerNotifications(user.getUserId());
		log.info("[getPlayerUnreadCount] userId={} unreadCount={}", user.getUserId(), count);
		return count;
	}

	// ── Shared ───────────────────────────────────────────────────────────────

	@Transactional
	public void markAsRead(String notificationId, User user) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new RessourceNotFoundException("Notification", "id", notificationId));
		if (!notification.getRecipient().getUserId().equals(user.getUserId())) {
			throw new IllegalStateException("You can only mark your own notifications as read");
		}
		notification.setRead(true);
		notificationRepository.save(notification);
	}

	@Transactional
	public void markAllAsRead(User user) {
		List<Notification> notifications = notificationRepository.findByRecipientUserId(user.getUserId());
		notifications.forEach(n -> n.setRead(true));
		notificationRepository.saveAll(notifications);
	}

	private NotificationResponse toResponse(Notification n) {
		String type = n.getNotificationType() != null ? n.getNotificationType().name() : NotificationType.PLAYER_APPLIED.name();
		return new NotificationResponse(n.getNotificationId(), n.getPlayerName(), n.getTrialLocation(),
				n.getTrialDate(), n.getTrialId(), n.isRead(), n.getCreatedAt(), type, n.getNewStatus());
	}

}
