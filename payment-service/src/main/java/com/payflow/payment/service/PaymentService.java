package com.payflow.payment.service;

import com.payflow.payment.domain.Payment;
import com.payflow.payment.domain.PaymentStatus;
import com.payflow.payment.dto.PaymentRequest;
import com.payflow.payment.dto.PaymentResponse;
import com.payflow.payment.event.PaymentEvent;
import com.payflow.payment.exception.PaymentNotFoundException;
import com.payflow.payment.kafka.PaymentKafkaProducer;
import com.payflow.payment.mapper.PaymentMapper;
import com.payflow.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentKafkaProducer kafkaProducer;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("Initiating payment from {} to {} amount {} {}",
            request.senderId(), request.receiverId(), request.amount(), request.currency());

        // Build and persist payment
        Payment payment = Payment.builder()
            .senderId(request.senderId())
            .receiverId(request.receiverId())
            .amount(request.amount())
            .currency(request.currency())
            .description(request.description())
            .status(PaymentStatus.PENDING)
            .build();

        payment = paymentRepository.save(payment);

        // Publish to Kafka for fraud detection
        PaymentEvent event = new PaymentEvent(
            payment.getId(),
            payment.getSenderId(),
            payment.getReceiverId(),
            payment.getAmount(),
            payment.getCurrency(),
            PaymentEvent.PAYMENT_INITIATED,
            Instant.now()
        );

        kafkaProducer.publishPaymentInitiated(event);

        // Update status to FRAUD_CHECK
        payment.setStatus(PaymentStatus.FRAUD_CHECK);
        payment = paymentRepository.save(payment);

        log.info("Payment {} initiated successfully, awaiting fraud check", payment.getId());
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsBySender(String senderId) {
        return paymentRepository.findBySenderIdOrderByCreatedAtDesc(senderId)
            .stream()
            .map(paymentMapper::toResponse)
            .toList();
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(UUID id, PaymentStatus status, String reason) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        payment.setStatus(status);
        if (reason != null) payment.setRejectionReason(reason);
        payment = paymentRepository.save(payment);

        // If completed, publish completion event
        if (status == PaymentStatus.COMPLETED) {
            PaymentEvent event = new PaymentEvent(
                payment.getId(),
                payment.getSenderId(),
                payment.getReceiverId(),
                payment.getAmount(),
                payment.getCurrency(),
                PaymentEvent.PAYMENT_COMPLETED,
                Instant.now()
            );
            kafkaProducer.publishPaymentCompleted(event);
        }

        log.info("Payment {} status updated to {}", id, status);
        return paymentMapper.toResponse(payment);
    }
}
