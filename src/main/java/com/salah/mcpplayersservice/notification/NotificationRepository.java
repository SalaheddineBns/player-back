package com.salah.mcpplayersservice.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, String> {

	List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(UUID userId);

	long countByRecipientUserIdAndIsReadFalse(UUID userId);

	List<Notification> findByRecipientUserId(UUID userId);

}
