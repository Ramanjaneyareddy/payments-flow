package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record InsightsRequest(
        long totalPayments,
        long approvedPayments,
        long rejectedPayments,
        long pendingPayments,
        long reviewPayments,

        @Schema(example = "4875000.00")
        BigDecimal totalAmount,

        @Schema(example = "4760.74")
        BigDecimal averageAmount,

        @Schema(example = "75000.00")
        BigDecimal maxAmount,

        @Schema(example = "0.50")
        BigDecimal minAmount
) {}