package com.payflow.payment.repository;

import com.payflow.payment.domain.Payment;
import com.payflow.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findBySenderIdOrderByCreatedAtDesc(String senderId);

    List<Payment> findByReceiverIdOrderByCreatedAtDesc(String receiverId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :from AND :to ORDER BY p.createdAt DESC")
    List<Payment> findByDateRange(Instant from, Instant to);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.senderId = :senderId AND p.status = 'PENDING'")
    long countPendingBySender(String senderId);
}
