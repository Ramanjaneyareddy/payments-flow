package com.payflow.inquiry.controller;

import com.payflow.inquiry.domain.PaymentStatus;
import com.payflow.inquiry.dto.*;
import com.payflow.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inquiry")
@RequiredArgsConstructor
@Tag(name = "Payment Inquiry")
@SecurityRequirement(name = "bearerAuth")
public class InquiryController {

    private final InquiryService inquiryService;

    @GetMapping("/payments/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentSummary> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(inquiryService.getById(id));
    }

    @GetMapping("/payments")
    @Operation(summary = "Get all payments (paginated)")
    public ResponseEntity<Page<PaymentSummary>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getAll(page, size));
    }

    @GetMapping("/payments/sender/{senderId}")
    public ResponseEntity<Page<PaymentSummary>> getBySender(
            @PathVariable String senderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getBySender(senderId, page, size));
    }

    @GetMapping("/payments/receiver/{receiverId}")
    public ResponseEntity<Page<PaymentSummary>> getByReceiver(
            @PathVariable String receiverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getByReceiver(receiverId, page, size));
    }

    @GetMapping("/payments/status/{status}")
    public ResponseEntity<Page<PaymentSummary>> getByStatus(
            @PathVariable PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getByStatus(status, page, size));
    }

    @GetMapping("/payments/date-range")
    @Operation(summary = "Search by ISO-8601 date range")
    public ResponseEntity<Page<PaymentSummary>> getByDateRange(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getByDateRange(Instant.parse(from), Instant.parse(to), page, size));
    }

    @GetMapping("/payments/amount-range")
    public ResponseEntity<Page<PaymentSummary>> getByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getByAmountRange(minAmount, maxAmount, page, size));
    }

    @GetMapping("/payments/advanced-search")
    @Operation(summary = "Multi-filter search (Optional params)")
    public ResponseEntity<Page<PaymentSummary>> advancedSearch(
            @RequestParam(required = false) String senderId,
            @RequestParam(required = false) String receiverId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.advancedSearch(
                senderId, receiverId, status, currency,
                from != null ? Instant.parse(from) : null,
                to != null ? Instant.parse(to) : null,
                page, size));
    }

    @GetMapping("/stats")
    public ResponseEntity<PaymentStats> getStats() {
        return ResponseEntity.ok(inquiryService.getStats());
    }

    @GetMapping("/stats/sender/{senderId}")
    public ResponseEntity<SenderStats> getSenderStats(@PathVariable String senderId) {
        return ResponseEntity.ok(new SenderStats(
                senderId,
                inquiryService.countBySender(senderId),
                inquiryService.totalAmountBySender(senderId)));
    }

    @GetMapping("/health")
    @Operation(summary = "Liveness probe", security = {})
    public String health() {
        return "Inquiry Service is running";
    }

    public record SenderStats(String senderId, long totalPayments, BigDecimal totalAmount) {}
}