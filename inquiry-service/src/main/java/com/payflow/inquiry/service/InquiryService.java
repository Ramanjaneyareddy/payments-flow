package com.payflow.inquiry.service;

import com.payflow.inquiry.domain.PaymentStatus;
import com.payflow.inquiry.dto.*;
import com.payflow.inquiry.exception.PaymentNotFoundException;
import com.payflow.inquiry.mapper.PaymentMapper;
import com.payflow.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository repository;
    private final PaymentMapper mapper;

    public PaymentSummary getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toSummary)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + id));
    }

    public Page<PaymentSummary> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.findAll(pageable).map(mapper::toSummary);
    }

    public Page<PaymentSummary> getBySender(String senderId, int page, int size) {
        return repository.findBySenderIdOrderByCreatedAtDesc(senderId, PageRequest.of(page, size))
                .map(mapper::toSummary);
    }

    public Page<PaymentSummary> getByStatus(PaymentStatus status, int page, int size) {
        return repository.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size))
                .map(mapper::toSummary);
    }

    public Page<PaymentSummary> getByDateRange(Instant from, Instant to, int page, int size) {
        return repository.findByDateRange(from, to, PageRequest.of(page, size))
                .map(mapper::toSummary);
    }

    public Page<PaymentSummary> advancedSearch(
            String senderId, String receiverId, PaymentStatus status,
            String currency, Instant from, Instant to, int page, int size) {
        log.info("Executing advanced search for sender: {}", senderId);
        return repository.advancedSearch(senderId, receiverId, status, currency, from, to, PageRequest.of(page, size))
                .map(mapper::toSummary);
    }

    public PaymentStats getStats() {
        return new PaymentStats(
                repository.count(),
                repository.countByStatus(PaymentStatus.APPROVED),
                repository.countByStatus(PaymentStatus.REJECTED),
                repository.countByStatus(PaymentStatus.PENDING) + repository.countByStatus(PaymentStatus.FRAUD_CHECK),
                repository.countByStatus(PaymentStatus.REVIEW),
                repository.totalAmount(),
                repository.averageAmount(),
                repository.maxAmount(),
                repository.minAmount()
        );
    }

    public long countBySender(String senderId) { return repository.countBySenderId(senderId); }

    public BigDecimal totalAmountBySender(String senderId) { return repository.totalAmountBySender(senderId); }
    // Add this back for getByReceiver
    public Page<PaymentSummary> getByReceiver(String receiverId, int page, int size) {
        return repository.findByReceiverIdOrderByCreatedAtDesc(receiverId, PageRequest.of(page, size))
                .map(mapper::toSummary);
    }

    // Add this back for getByAmountRange
    public Page<PaymentSummary> getByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, int page, int size) {
        return repository.findByAmountRange(minAmount, maxAmount, PageRequest.of(page, size))
                .map(mapper::toSummary);
    }
}