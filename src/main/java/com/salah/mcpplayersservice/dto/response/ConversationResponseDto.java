package com.salah.mcpplayersservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationResponseDto(UUID conversationId, UUID otherUserId, String otherUserName, String lastMessage,
		LocalDateTime lastMessageAt, long unreadCount) {
}
