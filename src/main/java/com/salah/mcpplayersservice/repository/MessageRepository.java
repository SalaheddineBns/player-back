package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

	Page<Message> findByConversationConversationIdOrderBySentAtDesc(UUID conversationId, Pageable pageable);

	@Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.conversationId = :convId AND m.receiver.userId = :userId AND m.read = false")
	long countUnread(@Param("convId") UUID conversationId, @Param("userId") UUID userId);

	@Modifying
	@Query("UPDATE Message m SET m.read = true WHERE m.conversation.conversationId = :convId AND m.receiver.userId = :userId AND m.read = false")
	void markAllAsRead(@Param("convId") UUID conversationId, @Param("userId") UUID userId);

}
