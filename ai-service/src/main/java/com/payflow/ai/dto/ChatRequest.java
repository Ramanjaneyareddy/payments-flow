package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank
        @Schema(example = "Why was my payment for EUR 75,000 rejected?")
        String message,

        @Schema(description = "Required for conversation history continuity", example = "session-abc-123")
        String sessionId
) {}