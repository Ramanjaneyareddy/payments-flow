package com.payflow.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Aggregate payment statistics across all payments")
public record PaymentStats(

    @Schema(description = "Total number of payments in the system", example = "1024")
    long totalPayments,

    @Schema(description = "Number of payments with status APPROVED", example = "850")
    long approvedPayments,

    @Schema(description = "Number of payments with status REJECTED", example = "95")
    long rejectedPayments,

    @Schema(description = "Number of payments with status PENDING or FRAUD_CHECK", example = "42")
    long pendingPayments,

    @Schema(description = "Number of payments with status REVIEW (manual review required)", example = "37")
    long reviewPayments,

    @Schema(description = "Sum of all payment amounts", example = "4875000.00")
    BigDecimal totalAmount,

    @Schema(description = "Average payment amount", example = "4760.74")
    BigDecimal averageAmount,

    @Schema(description = "Largest single payment amount", example = "75000.00")
    BigDecimal maxAmount,

    @Schema(description = "Smallest single payment amount", example = "0.50")
    BigDecimal minAmount

) {}
