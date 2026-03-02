package com.salah.mcpplayersservice.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, String> {

	// Manager notifications (PLAYER_APPLIED or legacy null)
	@Query("""
			SELECT n FROM Notification n WHERE n.recipient.userId = :userId
			AND (n.notificationType = 'PLAYER_APPLIED' OR n.notificationType IS NULL)
			ORDER BY n.createdAt DESC
			""")
	List<Notification> findManagerNotifications(@Param("userId") UUID userId);

	@Query("""
			SELECT COUNT(n) FROM Notification n WHERE n.recipient.userId = :userId
			AND (n.notificationType = 'PLAYER_APPLIED' OR n.notificationType IS NULL)
			AND n.isRead = false
			""")
	long countUnreadManagerNotifications(@Param("userId") UUID userId);

	// Player notifications (STATUS_CHANGED)
	@Query("""
			SELECT n FROM Notification n WHERE n.recipient.userId = :userId
			AND n.notificationType = 'STATUS_CHANGED'
			ORDER BY n.createdAt DESC
			""")
	List<Notification> findPlayerNotifications(@Param("userId") UUID userId);

	@Query("""
			SELECT COUNT(n) FROM Notification n WHERE n.recipient.userId = :userId
			AND n.notificationType = 'STATUS_CHANGED'
			AND n.isRead = false
			""")
	long countUnreadPlayerNotifications(@Param("userId") UUID userId);

	List<Notification> findByRecipientUserId(UUID userId);

}
