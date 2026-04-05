package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "AI-generated fraud decision explanation")
public record FraudExplainResponse(
    @Schema(description = "Payment UUID")
    UUID paymentId,

    @Schema(description = "Customer-facing plain-English explanation")
    String explanation,

    @Schema(description = "Actionable guidance for the customer")
    String customerGuidance,

    @Schema(description = "Technical note for the compliance team")
    String complianceNote,

    @Schema(description = "Risk level", example = "CRITICAL",
            allowableValues = {"LOW","MEDIUM","HIGH","CRITICAL"})
    String riskLevel
) {}
