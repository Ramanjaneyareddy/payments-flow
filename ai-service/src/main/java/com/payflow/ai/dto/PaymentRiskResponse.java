package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI risk assessment for a proposed payment")
public record PaymentRiskResponse(
    @Schema(description = "Risk rating", example = "HIGH",
            allowableValues = {"LOW","MEDIUM","HIGH","CRITICAL"})
    String riskRating,

    @Schema(description = "Predicted fraud score 0.0-1.0", example = "0.55")
    double predictedScore,

    @Schema(description = "Predicted decision", example = "REVIEW",
            allowableValues = {"APPROVED","REVIEW","REJECTED"})
    String predictedDecision,

    @Schema(description = "Risk narrative")
    String narrative,

    @Schema(description = "Identified risk factors")
    List<String> riskFactors,

    @Schema(description = "Recommendations to reduce risk")
    List<String> recommendations
) {}
