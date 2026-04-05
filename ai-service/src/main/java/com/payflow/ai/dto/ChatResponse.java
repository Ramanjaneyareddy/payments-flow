package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI assistant reply")
public record ChatResponse(
    @Schema(description = "AI response text")
    String reply,

    @Schema(description = "Session ID for conversation continuity")
    String sessionId
) {}
