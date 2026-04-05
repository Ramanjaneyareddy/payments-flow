package com.payflow.payment.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
    UUID paymentId,
    String senderId,
    String receiverId,
    BigDecimal amount,
    String currency,
    String eventType,
    Instant occurredAt
) {
    public static final String PAYMENT_INITIATED = "PAYMENT_INITIATED";
    public static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    public static final String PAYMENT_REJECTED  = "PAYMENT_REJECTED";
}
