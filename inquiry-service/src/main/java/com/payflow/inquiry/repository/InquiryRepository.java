package com.payflow.inquiry.repository;

import com.payflow.inquiry.domain.Payment;
import com.payflow.inquiry.domain.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface InquiryRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findBySenderIdOrderByCreatedAtDesc(String senderId, Pageable pageable);
    Page<Payment> findByReceiverIdOrderByCreatedAtDesc(String receiverId, Pageable pageable);
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);
    Page<Payment> findBySenderIdAndStatusOrderByCreatedAtDesc(String senderId, PaymentStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :from AND :to")
    Page<Payment> findByDateRange(Instant from, Instant to, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :min AND :max")
    Page<Payment> findByAmountRange(BigDecimal min, BigDecimal max, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Payment> searchByDescription(String keyword, Pageable pageable);

    @Query("""
           SELECT p FROM Payment p WHERE 
           (:senderId IS NULL OR p.senderId = :senderId) AND 
           (:receiverId IS NULL OR p.receiverId = :receiverId) AND 
           (:status IS NULL OR p.status = :status) AND 
           (:currency IS NULL OR p.currency = :currency) AND 
           (:from IS NULL OR p.createdAt >= :from) AND 
           (:to IS NULL OR p.createdAt <= :to)
           """)
    Page<Payment> advancedSearch(String senderId, String receiverId, PaymentStatus status,
                                 String currency, Instant from, Instant to, Pageable pageable);

    // Analytical Aggregations
    long countBySenderId(String senderId);
    long countByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.senderId = :senderId AND p.status = 'COMPLETED'")
    BigDecimal totalAmountBySender(String senderId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    BigDecimal totalAmount();

    @Query("SELECT COALESCE(AVG(p.amount), 0) FROM Payment p")
    BigDecimal averageAmount();

    @Query("SELECT COALESCE(MAX(p.amount), 0) FROM Payment p")
    BigDecimal maxAmount();

    @Query("SELECT COALESCE(MIN(p.amount), 0) FROM Payment p")
    BigDecimal minAmount();
}