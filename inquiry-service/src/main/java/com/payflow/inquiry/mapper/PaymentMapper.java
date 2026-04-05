package com.payflow.inquiry.mapper;

import com.payflow.inquiry.domain.Payment;
import com.payflow.inquiry.dto.PaymentSummary;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentSummary toSummary(Payment payment) {
        return new PaymentSummary(
            payment.getId(),
            payment.getSenderId(),
            payment.getReceiverId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getStatus(),
            payment.getDescription(),
            payment.getFraudScore(),
            payment.getRejectionReason(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }
}
