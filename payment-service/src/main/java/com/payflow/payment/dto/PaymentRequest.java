package com.payflow.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "Request payload to initiate a new payment")
public record PaymentRequest(

    @Schema(description = "Unique identifier of the payment sender",
            example = "user-abc-123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Sender ID is required")
    String senderId,

    @Schema(description = "Unique identifier of the payment receiver",
            example = "user-xyz-456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Receiver ID is required")
    String receiverId,

    @Schema(description = "Payment amount. Must be between 0.01 and 1,000,000.00",
            example = "250.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "1000000.00", message = "Amount exceeds maximum limit of 1,000,000")
    BigDecimal amount,

    @Schema(description = "ISO 4217 3-letter currency code",
            example = "EUR", minLength = 3, maxLength = 3,
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    String currency,

    @Schema(description = "Optional human-readable description of the payment",
            example = "Invoice #INV-2024-001 payment", maxLength = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description

) {}
