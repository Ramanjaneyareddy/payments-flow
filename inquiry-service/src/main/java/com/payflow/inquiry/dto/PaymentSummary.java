package com.payflow.inquiry.dto;

import com.payflow.inquiry.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Payment summary returned by the Inquiry Service")
public record PaymentSummary(

    @Schema(description = "Unique payment identifier (UUID)",
            example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Sender identifier", example = "user-abc-123")
    String senderId,

    @Schema(description = "Receiver identifier", example = "user-xyz-456")
    String receiverId,

    @Schema(description = "Payment amount", example = "250.00")
    BigDecimal amount,

    @Schema(description = "ISO 4217 currency code", example = "EUR")
    String currency,

    @Schema(description = "Current lifecycle status", example = "COMPLETED")
    PaymentStatus status,

    @Schema(description = "Payment description", example = "Invoice #INV-2024-001 payment")
    String description,

    @Schema(description = "Fraud risk score (0.0 – 1.0)", example = "0.15")
    String fraudScore,

    @Schema(description = "Rejection reason (only present when status is REJECTED)",
            example = "Sender is on the dynamic blacklist")
    String rejectionReason,

    @Schema(description = "ISO-8601 creation timestamp", example = "2024-03-15T10:30:00Z")
    Instant createdAt,

    @Schema(description = "ISO-8601 last-updated timestamp", example = "2024-03-15T10:30:05Z")
    Instant updatedAt

) {}
