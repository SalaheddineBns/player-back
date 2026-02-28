package com.salah.mcpplayersservice.dto.request;

import java.util.UUID;

public record SendMessageRequest(UUID receiverId, String content) {
}
