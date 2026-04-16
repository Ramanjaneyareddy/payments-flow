package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SenderSummaryResponse(
        String senderId,

        @Schema(allowableValues = {"TRUSTED", "STANDARD", "ELEVATED", "HIGH_RISK"})
        String riskProfile,

        @Schema(description = "Natural language summary of the sender's history")
        String behaviourSummary,

        List<String> patterns,
        List<String> recommendations
) {}