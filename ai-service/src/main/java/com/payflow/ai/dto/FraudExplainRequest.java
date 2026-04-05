package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to explain a fraud decision in natural language")
public record FraudExplainRequest(
    @Schema(description = "Payment UUID", required = true,
            example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull UUID paymentId,

    @Schema(description = "Fraud decision", example = "REJECTED")
    @NotBlank String decision,

    @Schema(description = "Risk score 0.0-1.0", example = "0.95")
    double score,

    @Schema(description = "Triggered fraud rules",
            example = "[\"BLACKLIST_RULE\"]")
    List<String> triggeredRules,

    @Schema(description = "Raw reason from fraud engine",
            example = "Sender blocked-user-001 is on the static blacklist")
    String rawReason,

    @Schema(description = "Payment amount", example = "250.00")
    BigDecimal amount,

    @Schema(description = "ISO 4217 currency code", example = "EUR")
    String currency,

    @Schema(description = "Sender identifier", example = "user-abc-123")
    String senderId
) {}
