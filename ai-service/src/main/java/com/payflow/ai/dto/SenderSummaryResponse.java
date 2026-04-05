package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI-generated sender behavioural profile")
public record SenderSummaryResponse(
    @Schema(description = "Sender identifier")
    String senderId,

    @Schema(description = "Risk profile", example = "TRUSTED",
            allowableValues = {"TRUSTED","STANDARD","ELEVATED","HIGH_RISK"})
    String riskProfile,

    @Schema(description = "Behavioural summary narrative")
    String behaviourSummary,

    @Schema(description = "Notable payment patterns")
    List<String> patterns,

    @Schema(description = "Recommended actions")
    List<String> recommendations
) {}
