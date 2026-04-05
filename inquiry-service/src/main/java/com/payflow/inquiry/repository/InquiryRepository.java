package com.payflow.inquiry.repository;

import com.payflow.inquiry.domain.Payment;
import com.payflow.inquiry.domain.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface InquiryRepository extends JpaRepository<Payment, UUID> {

    // Search by sender
    Page<Payment> findBySenderIdOrderByCreatedAtDesc(String senderId, Pageable pageable);

    // Search by receiver
    Page<Payment> findByReceiverIdOrderByCreatedAtDesc(String receiverId, Pageable pageable);

    // Search by status
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    // Search by sender and status
    Page<Payment> findBySenderIdAndStatusOrderByCreatedAtDesc(
        String senderId, PaymentStatus status, Pageable pageable);

    // Search by date range
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :from AND :to ORDER BY p.createdAt DESC")
    Page<Payment> findByDateRange(
        @Param("from") Instant from,
        @Param("to") Instant to,
        Pageable pageable);

    // Search by amount range
    @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :minAmount AND :maxAmount ORDER BY p.createdAt DESC")
    Page<Payment> findByAmountRange(
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        Pageable pageable);

    // Full text search by description
    @Query("SELECT p FROM Payment p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.createdAt DESC")
    Page<Payment> searchByDescription(
        @Param("keyword") String keyword,
        Pageable pageable);

    // Advanced search - multiple optional filters
    @Query("SELECT p FROM Payment p WHERE " +
           "(:senderId IS NULL OR p.senderId = :senderId) AND " +
           "(:receiverId IS NULL OR p.receiverId = :receiverId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:currency IS NULL OR p.currency = :currency) AND " +
           "(:from IS NULL OR p.createdAt >= :from) AND " +
           "(:to IS NULL OR p.createdAt <= :to) " +
           "ORDER BY p.createdAt DESC")
    Page<Payment> advancedSearch(
        @Param("senderId") String senderId,
        @Param("receiverId") String receiverId,
        @Param("status") PaymentStatus status,
        @Param("currency") String currency,
        @Param("from") Instant from,
        @Param("to") Instant to,
        Pageable pageable);

    // Stats queries
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.senderId = :senderId")
    long countBySenderId(@Param("senderId") String senderId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.senderId = :senderId AND p.status = 'COMPLETED'")
    BigDecimal totalAmountBySender(@Param("senderId") String senderId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    BigDecimal totalAmount();

    @Query("SELECT COALESCE(AVG(p.amount), 0) FROM Payment p")
    BigDecimal averageAmount();

    @Query("SELECT COALESCE(MAX(p.amount), 0) FROM Payment p")
    BigDecimal maxAmount();

    @Query("SELECT COALESCE(MIN(p.amount), 0) FROM Payment p")
    BigDecimal minAmount();
}
