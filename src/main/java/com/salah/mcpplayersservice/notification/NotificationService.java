package com.salah.mcpplayersservice.notification;

import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	@Transactional
	public void createNotification(User recipient, String playerName, String trialLocation, LocalDateTime trialDate,
			String trialId) {
		Notification notification = Notification.builder()
			.recipient(recipient)
			.playerName(playerName)
			.trialLocation(trialLocation)
			.trialDate(trialDate)
			.trialId(trialId)
			.build();
		notificationRepository.save(notification);
	}

	public List<NotificationResponse> getNotifications(User user) {
		return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(user.getUserId())
			.stream()
			.map(this::toResponse)
			.toList();
	}

	public long getUnreadCount(User user) {
		return notificationRepository.countByRecipientUserIdAndIsReadFalse(user.getUserId());
	}

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
		return new NotificationResponse(n.getNotificationId(), n.getPlayerName(), n.getTrialLocation(),
				n.getTrialDate(), n.getTrialId(), n.isRead(), n.getCreatedAt());
	}

}
