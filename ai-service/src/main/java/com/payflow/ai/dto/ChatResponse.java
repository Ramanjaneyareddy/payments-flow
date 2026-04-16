package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatResponse(
        String reply,

        @Schema(description = "Session ID for multi-turn continuity")
        String sessionId
) {}