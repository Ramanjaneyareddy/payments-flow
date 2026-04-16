package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record FraudExplainRequest(
        @NotNull
        UUID paymentId,

        @NotBlank
        @Schema(example = "REJECTED")
        String decision,

        @Schema(example = "0.95")
        double score,

        @Schema(example = "[\"BLACKLIST_RULE\"]")
        List<String> triggeredRules,

        @Schema(description = "The technical reason from the fraud engine", example = "Sender is on the static blacklist")
        String rawReason,

        @Schema(example = "250.00")
        BigDecimal amount,

        @Schema(example = "EUR")
        String currency,

        String senderId
) {}