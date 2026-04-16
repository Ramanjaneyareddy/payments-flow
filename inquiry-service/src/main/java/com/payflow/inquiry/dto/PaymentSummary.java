package com.payflow.inquiry.dto;

import com.payflow.inquiry.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Summary of payment transaction details")
public record PaymentSummary(
        UUID id,
        String senderId,
        String receiverId,

        @Schema(example = "250.00")
        BigDecimal amount,

        @Schema(example = "EUR")
        String currency,

        PaymentStatus status,
        String description,

        @Schema(description = "0.0 - 1.0 range")
        String fraudScore,

        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {}