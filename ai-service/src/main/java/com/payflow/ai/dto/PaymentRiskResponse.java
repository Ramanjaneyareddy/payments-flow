package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PaymentRiskResponse(
        @Schema(allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
        String riskRating,

        @Schema(example = "0.55")
        double predictedScore,

        @Schema(allowableValues = {"APPROVED", "REVIEW", "REJECTED"})
        String predictedDecision,

        String narrative,
        List<String> riskFactors,
        List<String> recommendations
) {}