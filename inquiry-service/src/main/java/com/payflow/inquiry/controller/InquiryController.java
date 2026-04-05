package com.payflow.inquiry.controller;

import com.payflow.inquiry.domain.PaymentStatus;
import com.payflow.inquiry.dto.PaymentStats;
import com.payflow.inquiry.dto.PaymentSummary;
import com.payflow.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inquiry")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Inquiry", description = "Search and retrieve payment history (read-only)")
@SecurityRequirement(name = "bearerAuth")
public class InquiryController {

    private final InquiryService inquiryService;

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/payments/{id}
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/payments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get payment by ID",
        description = "Returns full payment details for the given UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment found",
            content = @Content(schema = @Schema(implementation = PaymentSummary.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<PaymentSummary> getById(
            @Parameter(description = "UUID of the payment", required = true,
                       example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(inquiryService.getById(id));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/payments
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/payments", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get all payments (paginated)",
        description = "Returns all payments ordered by creation date descending. Supports pagination.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated list of payments"),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<Page<PaymentSummary>> getAll(
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, max recommended 100)", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getAll(page, size));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/payments/sender/{senderId}
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/payments/sender/{senderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get payments by sender",
        description = "Returns all payments initiated by the specified sender, ordered by date descending.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated list of payments"),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<Page<PaymentSummary>> getBySender(
            @Parameter(description = "Sender identifier", required = true, example = "user-abc-123")
            @PathVariable String senderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getBySender(senderId, page, size));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/payments/receiver/{receiverId}
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/payments/receiver/{receiverId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get payments by receiver",
        description = "Returns all payments received by the specified receiver, ordered by date descending.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated list of payments"),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<Page<PaymentSummary>> getByReceiver(
            @Parameter(description = "Receiver identifier", required = true, example = "user-xyz-456")
            @PathVariable String receiverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getByReceiver(receiverId, page, size));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/payments/status/{status}
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/payments/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get payments by status",
        description = "Filters payments by lifecycle status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated list of payments"),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<Page<PaymentSummary>> getByStatus(
            @Parameter(description = "Payment status",
                       schema = @Schema(implementation = PaymentStatus.class),
                       required = true, example = "APPROVED")
            @PathVariable PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getByStatus(status, page, size));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/payments/sender/{senderId}/status/{status}
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/payments/sender/{senderId}/status/{status}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get payments by sender and status",
        description = "Filters payments by both sender and lifecycle status.")
    @ApiResponse(responseCode = "200", description = "Paginated list of payments")
    public ResponseEntity<Page<PaymentSummary>> getBySenderAndStatus(
            @Parameter(description = "Sender identifier", required = true, example = "user-abc-123")
            @PathVariable String senderId,
            @Parameter(description = "Payment status", required = true, example = "COMPLETED")
            @PathVariable PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getBySenderAndStatus(senderId, status, page, size));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/payments/date-range
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/payments/date-range", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get payments within a date range",
        description = "Returns payments created between `from` and `to` (inclusive). "
                    + "Dates must be ISO-8601 format, e.g. `2024-01-01T00:00:00Z`.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated list of payments"),
        @ApiResponse(responseCode = "400", description = "Invalid date format")
    })
    public ResponseEntity<Page<PaymentSummary>> getByDateRange(
            @Parameter(description = "Start date/time (ISO-8601)", required = true,
                       example = "2024-01-01T00:00:00Z")
            @RequestParam String from,
            @Parameter(description = "End date/time (ISO-8601)", required = true,
                       example = "2024-03-31T23:59:59Z")
            @RequestParam String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
            inquiryService.getByDateRange(Instant.parse(from), Instant.parse(to), page, size));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/payments/amount-range
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/payments/amount-range", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get payments within an amount range",
        description = "Returns payments where the amount falls between `minAmount` and `maxAmount`.")
    @ApiResponse(responseCode = "200", description = "Paginated list of payments")
    public ResponseEntity<Page<PaymentSummary>> getByAmountRange(
            @Parameter(description = "Minimum amount (inclusive)", required = true, example = "100.00")
            @RequestParam BigDecimal minAmount,
            @Parameter(description = "Maximum amount (inclusive)", required = true, example = "5000.00")
            @RequestParam BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
            inquiryService.getByAmountRange(minAmount, maxAmount, page, size));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/payments/search
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/payments/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Search payments by description keyword",
        description = "Full-text search on the payment description field (case-insensitive).")
    @ApiResponse(responseCode = "200", description = "Paginated list of matching payments")
    public ResponseEntity<Page<PaymentSummary>> searchByDescription(
            @Parameter(description = "Search keyword", required = true, example = "invoice")
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.searchByDescription(keyword, page, size));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/payments/advanced-search  (FIX #15 — was missing from README)
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/payments/advanced-search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Advanced multi-filter search",
        description = """
            Flexible search endpoint supporting multiple optional filters combined with AND logic.
            
            All parameters are optional — omit any to treat it as a wildcard.
            
            Example: find all COMPLETED EUR payments from a specific sender in Q1 2024:
            ```
            /payments/advanced-search?senderId=user-abc-123&status=COMPLETED&currency=EUR
              &from=2024-01-01T00:00:00Z&to=2024-03-31T23:59:59Z
            ```
            """)
    @ApiResponse(responseCode = "200", description = "Paginated list of matching payments")
    public ResponseEntity<Page<PaymentSummary>> advancedSearch(
            @Parameter(description = "Filter by sender ID", example = "user-abc-123")
            @RequestParam(required = false) String senderId,
            @Parameter(description = "Filter by receiver ID", example = "user-xyz-456")
            @RequestParam(required = false) String receiverId,
            @Parameter(description = "Filter by payment status", example = "COMPLETED")
            @RequestParam(required = false) PaymentStatus status,
            @Parameter(description = "Filter by ISO 4217 currency code", example = "EUR")
            @RequestParam(required = false) String currency,
            @Parameter(description = "Start date/time (ISO-8601)", example = "2024-01-01T00:00:00Z")
            @RequestParam(required = false) String from,
            @Parameter(description = "End date/time (ISO-8601)", example = "2024-03-31T23:59:59Z")
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.advancedSearch(
            senderId, receiverId, status, currency,
            from != null ? Instant.parse(from) : null,
            to   != null ? Instant.parse(to)   : null,
            page, size));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/stats
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get overall payment statistics",
        description = "Returns aggregate statistics: total counts by status, sum/avg/min/max amounts.")
    @ApiResponse(responseCode = "200", description = "Payment statistics",
        content = @Content(schema = @Schema(implementation = PaymentStats.class)))
    public ResponseEntity<PaymentStats> getStats() {
        return ResponseEntity.ok(inquiryService.getStats());
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/stats/sender/{senderId}  (FIX #15 — was missing from README)
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/stats/sender/{senderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get statistics for a specific sender",
        description = "Returns total payment count and total completed amount for the given sender.")
    @ApiResponse(responseCode = "200", description = "Sender statistics")
    public ResponseEntity<SenderStats> getSenderStats(
            @Parameter(description = "Sender identifier", required = true, example = "user-abc-123")
            @PathVariable String senderId) {
        return ResponseEntity.ok(new SenderStats(
            senderId,
            inquiryService.countBySender(senderId),
            inquiryService.totalAmountBySender(senderId)));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/inquiry/health
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
        summary  = "Service health check",
        description = "Lightweight liveness probe.",
        security = {})  // no auth required
    @ApiResponse(responseCode = "200", description = "Service is running",
        content = @Content(schema = @Schema(type = "string",
                           example = "Inquiry Service is running")))
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inquiry Service is running");
    }

    // ─────────────────────────────────────────────────────────────────
    // Internal response record
    // ─────────────────────────────────────────────────────────────────
    @Schema(description = "Aggregate payment statistics for a specific sender")
    record SenderStats(
        @Schema(description = "Sender identifier", example = "user-abc-123")
        String senderId,
        @Schema(description = "Total number of payments initiated", example = "42")
        long totalPayments,
        @Schema(description = "Total amount of COMPLETED payments", example = "18500.00")
        java.math.BigDecimal totalAmount
    ) {}
}
