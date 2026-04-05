package com.payflow.fraud.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FraudResult(
    UUID paymentId,
    FraudDecision decision,
    double score,
    List<String> triggeredRules,
    String reason,
    Instant evaluatedAt
) {
    public enum FraudDecision {
        APPROVED, REVIEW, REJECTED
    }
}
