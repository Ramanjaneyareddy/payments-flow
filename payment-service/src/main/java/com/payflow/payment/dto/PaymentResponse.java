package com.payflow.payment.dto;

import com.payflow.payment.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Payment details returned by the Payment Service")
public record PaymentResponse(
        UUID id,
        String senderId,
        String receiverId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String description,
        @Schema(description = "Fraud risk score (0.0 – 1.0)", example = "0.15")
        String fraudScore,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {}