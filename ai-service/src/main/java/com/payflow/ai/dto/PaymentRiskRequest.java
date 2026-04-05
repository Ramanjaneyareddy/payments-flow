package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request to assess risk of a proposed payment before submission")
public record PaymentRiskRequest(
    @Schema(description = "Sender identifier", example = "user-abc-123", required = true)
    @NotBlank String senderId,

    @Schema(description = "Receiver identifier", example = "user-xyz-456", required = true)
    @NotBlank String receiverId,

    @Schema(description = "Proposed payment amount", example = "15000.00", required = true)
    @NotNull BigDecimal amount,

    @Schema(description = "ISO 4217 currency code", example = "EUR", required = true)
    @NotBlank String currency,

    @Schema(description = "Optional description", example = "Quarterly supplier payment")
    String description
) {}
