package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record InsightsResponse(
        @Schema(allowableValues = {"HEALTHY", "ATTENTION", "ALERT", "CRITICAL"})
        String portfolioHealth,

        @Schema(example = "9.28")
        double rejectionRatePercent,

        List<String> keyFindings,
        List<String> recommendations,
        String executiveSummary
) {}