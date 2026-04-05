package com.payflow.inquiry.domain;

public enum PaymentStatus {
    PENDING,
    FRAUD_CHECK,
    APPROVED,
    REJECTED,
    COMPLETED,
    FAILED,
    REVIEW
}
