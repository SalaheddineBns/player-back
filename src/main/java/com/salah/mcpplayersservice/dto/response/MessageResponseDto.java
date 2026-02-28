package com.salah.mcpplayersservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponseDto(UUID messageId, UUID senderId, String senderName, UUID receiverId, String receiverName,
		String content, LocalDateTime sentAt, boolean read) {
}
