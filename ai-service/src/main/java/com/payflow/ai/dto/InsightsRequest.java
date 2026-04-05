package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Payment portfolio statistics for AI analysis")
public record InsightsRequest(
    @Schema(description = "Total number of payments", example = "1024")
    long totalPayments,

    @Schema(description = "Number of approved payments", example = "850")
    long approvedPayments,

    @Schema(description = "Number of rejected payments", example = "95")
    long rejectedPayments,

    @Schema(description = "Number pending/in fraud-check", example = "42")
    long pendingPayments,

    @Schema(description = "Number flagged for review", example = "37")
    long reviewPayments,

    @Schema(description = "Total payment volume", example = "4875000.00")
    BigDecimal totalAmount,

    @Schema(description = "Average payment amount", example = "4760.74")
    BigDecimal averageAmount,

    @Schema(description = "Maximum single payment", example = "75000.00")
    BigDecimal maxAmount,

    @Schema(description = "Minimum single payment", example = "0.50")
    BigDecimal minAmount
) {}
