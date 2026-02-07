package com.salah.mcpplayersservice.dto.response;

public record ErrorResponseDto(int status, String message, long timestamp) {
}
