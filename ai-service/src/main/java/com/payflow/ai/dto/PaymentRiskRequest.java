package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRiskRequest(
        @NotBlank
        @Schema(example = "user-abc-123")
        String senderId,

        @NotBlank
        @Schema(example = "user-xyz-456")
        String receiverId,

        @NotNull
        @Schema(example = "15000.00")
        BigDecimal amount,

        @NotBlank
        @Schema(example = "EUR")
        String currency,

        @Schema(example = "Quarterly supplier invoice")
        String description
) {}