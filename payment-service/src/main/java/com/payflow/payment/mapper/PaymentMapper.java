package com.payflow.payment.mapper;

import com.payflow.payment.domain.Payment;
import com.payflow.payment.domain.PaymentStatus;
import com.payflow.payment.dto.PaymentRequest;
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

    public Payment toEntity(PaymentRequest request) {
        return Payment.builder()
                .senderId(request.senderId())
                .receiverId(request.receiverId())
                .amount(request.amount())
                .currency(request.currency())
                .description(request.description())
                .status(PaymentStatus.PENDING)
                .build();
    }
}
