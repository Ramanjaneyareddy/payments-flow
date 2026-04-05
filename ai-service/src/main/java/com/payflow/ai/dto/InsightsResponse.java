package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI-generated portfolio insights")
public record InsightsResponse(
    @Schema(description = "Portfolio health", example = "HEALTHY",
            allowableValues = {"HEALTHY","ATTENTION","ALERT","CRITICAL"})
    String portfolioHealth,

    @Schema(description = "Rejection rate as percentage", example = "9.28")
    double rejectionRatePercent,

    @Schema(description = "Key findings and anomalies")
    List<String> keyFindings,

    @Schema(description = "Actionable recommendations")
    List<String> recommendations,

    @Schema(description = "Executive summary")
    String executiveSummary
) {}
