package com.payflow.payment.dto;

import com.payflow.payment.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Payment details returned by the Payment Service")
public record PaymentResponse(

    @Schema(description = "Unique payment identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Sender identifier", example = "user-abc-123")
    String senderId,

    @Schema(description = "Receiver identifier", example = "user-xyz-456")
    String receiverId,

    @Schema(description = "Payment amount", example = "250.00")
    BigDecimal amount,

    @Schema(description = "ISO 4217 currency code", example = "EUR")
    String currency,

    @Schema(description = """
        Current lifecycle status of the payment:
        - `PENDING`     — just created, not yet submitted for fraud check
        - `FRAUD_CHECK` — submitted to fraud-detection-service
        - `APPROVED`    — fraud check passed, awaiting settlement
        - `COMPLETED`   — successfully settled
        - `REJECTED`    — blocked by fraud detection
        - `REVIEW`      — flagged for manual review
        - `FAILED`      — processing error
        """,
        example = "FRAUD_CHECK")
    PaymentStatus status,

    @Schema(description = "Optional payment description", example = "Invoice #INV-2024-001 payment")
    String description,

    @Schema(description = "Fraud risk score returned by the fraud-detection-service (0.0 – 1.0)",
            example = "0.15")
    String fraudScore,

    @Schema(description = "Reason for rejection (populated only when status is REJECTED)",
            example = "Sender is on the dynamic blacklist")
    String rejectionReason,

    @Schema(description = "ISO-8601 timestamp of payment creation", example = "2024-03-15T10:30:00Z")
    Instant createdAt,

    @Schema(description = "ISO-8601 timestamp of last status update", example = "2024-03-15T10:30:05Z")
    Instant updatedAt

) {}
