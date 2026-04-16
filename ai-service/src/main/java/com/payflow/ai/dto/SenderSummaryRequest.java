package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Map;

public record SenderSummaryRequest(
        @NotBlank
        @Schema(example = "user-abc-123")
        String senderId,

        long totalPayments,

        @Schema(example = "18500.00")
        BigDecimal totalAmount,

        @Schema(description = "Count per status", example = "{\"COMPLETED\": 35, \"REJECTED\": 5}")
        Map<String, Long> statusBreakdown
) {}