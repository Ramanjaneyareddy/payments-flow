package com.payflow.inquiry.service;

import com.payflow.inquiry.domain.PaymentStatus;
import com.payflow.inquiry.dto.PaymentStats;
import com.payflow.inquiry.dto.PaymentSummary;
import com.payflow.inquiry.exception.PaymentNotFoundException;
import com.payflow.inquiry.mapper.PaymentMapper;
import com.payflow.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // Get single payment by ID
    public PaymentSummary getById(UUID id) {
        log.info("Fetching payment by id: {}", id);
        return repository.findById(id)
            .map(mapper::toSummary)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + id));
    }

    // Get all payments with pagination
    public Page<PaymentSummary> getAll(int page, int size) {
        log.info("Fetching all payments page={} size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.findAll(pageable).map(mapper::toSummary);
    }

    // Search by sender ID
    public Page<PaymentSummary> getBySender(String senderId, int page, int size) {
        log.info("Fetching payments for sender: {}", senderId);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findBySenderIdOrderByCreatedAtDesc(senderId, pageable)
            .map(mapper::toSummary);
    }

    // Search by receiver ID
    public Page<PaymentSummary> getByReceiver(String receiverId, int page, int size) {
        log.info("Fetching payments for receiver: {}", receiverId);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByReceiverIdOrderByCreatedAtDesc(receiverId, pageable)
            .map(mapper::toSummary);
    }

    // Search by status
    public Page<PaymentSummary> getByStatus(PaymentStatus status, int page, int size) {
        log.info("Fetching payments with status: {}", status);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByStatusOrderByCreatedAtDesc(status, pageable)
            .map(mapper::toSummary);
    }

    // Search by sender + status
    public Page<PaymentSummary> getBySenderAndStatus(
            String senderId, PaymentStatus status, int page, int size) {
        log.info("Fetching payments for sender: {} status: {}", senderId, status);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findBySenderIdAndStatusOrderByCreatedAtDesc(senderId, status, pageable)
            .map(mapper::toSummary);
    }

    // Search by date range
    public Page<PaymentSummary> getByDateRange(Instant from, Instant to, int page, int size) {
        log.info("Fetching payments between {} and {}", from, to);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByDateRange(from, to, pageable).map(mapper::toSummary);
    }

    // Search by amount range
    public Page<PaymentSummary> getByAmountRange(
            BigDecimal minAmount, BigDecimal maxAmount, int page, int size) {
        log.info("Fetching payments between {} and {}", minAmount, maxAmount);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByAmountRange(minAmount, maxAmount, pageable)
            .map(mapper::toSummary);
    }

    // Search by description keyword
    public Page<PaymentSummary> searchByDescription(String keyword, int page, int size) {
        log.info("Searching payments by keyword: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        return repository.searchByDescription(keyword, pageable).map(mapper::toSummary);
    }

    // Advanced search with multiple optional filters
    public Page<PaymentSummary> advancedSearch(
            String senderId, String receiverId, PaymentStatus status,
            String currency, Instant from, Instant to, int page, int size) {
        log.info("Advanced search: sender={} receiver={} status={} currency={}",
            senderId, receiverId, status, currency);
        Pageable pageable = PageRequest.of(page, size);
        return repository.advancedSearch(senderId, receiverId, status, currency, from, to, pageable)
            .map(mapper::toSummary);
    }

    // Get payment statistics
    public PaymentStats getStats() {
        log.info("Fetching payment statistics");
        return new PaymentStats(
            repository.count(),
            repository.countByStatus(PaymentStatus.APPROVED),
            repository.countByStatus(PaymentStatus.REJECTED),
            repository.countByStatus(PaymentStatus.PENDING) +
                repository.countByStatus(PaymentStatus.FRAUD_CHECK),
            repository.countByStatus(PaymentStatus.REVIEW),
            repository.totalAmount(),
            repository.averageAmount(),
            repository.maxAmount(),
            repository.minAmount()
        );
    }

    // Get stats for a specific sender
    public long countBySender(String senderId) {
        return repository.countBySenderId(senderId);
    }

    public BigDecimal totalAmountBySender(String senderId) {
        return repository.totalAmountBySender(senderId);
    }
}
