package com.payflow.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Request to summarise a sender's payment behaviour")
public record SenderSummaryRequest(
    @Schema(description = "Sender identifier", required = true, example = "user-abc-123")
    @NotBlank String senderId,

    @Schema(description = "Total number of payments sent", example = "42")
    long totalPayments,

    @Schema(description = "Total completed amount", example = "18500.00")
    BigDecimal totalAmount,

    @Schema(description = "Distribution of payment statuses",
            example = "{\"COMPLETED\": 35, \"REJECTED\": 5}")
    Map<String, Long> statusBreakdown
) {}
