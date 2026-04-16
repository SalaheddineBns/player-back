package com.salah.mcpplayersservice.dto.response;

import java.util.UUID;

public record RequiredDocumentResponseDto(UUID documentId, String title, String description) {
}
