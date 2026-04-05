package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Conversational chat message")
public record ChatRequest(
    @Schema(description = "User message", required = true,
            example = "Why was my payment for EUR 75,000 rejected?")
    @NotBlank String message,

    @Schema(description = "Session ID for multi-turn conversation continuity",
            example = "session-abc-123")
    String sessionId
) {}
