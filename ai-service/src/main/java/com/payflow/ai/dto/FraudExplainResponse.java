package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record FraudExplainResponse(
        UUID paymentId,

        @Schema(description = "Natural language explanation for the customer")
        String explanation,

        @Schema(description = "Suggested next steps")
        String customerGuidance,

        @Schema(description = "Internal compliance context")
        String complianceNote,

        @Schema(allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
        String riskLevel
) {}