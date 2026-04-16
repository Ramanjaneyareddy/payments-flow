package com.payflow.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "Request payload to initiate a new payment")
public record PaymentRequest(
        @Schema(example = "user-abc-123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Sender ID is required")
        String senderId,

        @Schema(example = "user-xyz-456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Receiver ID is required")
        String receiverId,

        @Schema(example = "250.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01")
        @DecimalMax(value = "1000000.00")
        BigDecimal amount,

        @Schema(example = "EUR", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 3, max = 3)
        String currency,

        @Schema(example = "Invoice payment")
        @Size(max = 500)
        String description
) {}