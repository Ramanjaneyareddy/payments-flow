package com.payflow.payment.domain;

public enum PaymentStatus {
    PENDING,
    FRAUD_CHECK,
    APPROVED,
    REJECTED,
    COMPLETED,
    FAILED
}
