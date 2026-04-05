package com.payflow.fraud.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentEvent(
    UUID paymentId,
    String senderId,
    String receiverId,
    BigDecimal amount,
    String currency,
    String eventType,
    Instant occurredAt
) {}
