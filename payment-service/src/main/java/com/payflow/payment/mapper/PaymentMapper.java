package com.payflow.payment.mapper;

import com.payflow.payment.domain.Payment;
import com.payflow.payment.dto.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
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
