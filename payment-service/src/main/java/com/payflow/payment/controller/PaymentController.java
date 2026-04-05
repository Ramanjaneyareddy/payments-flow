package com.payflow.payment.controller;

import com.payflow.payment.dto.PaymentRequest;
import com.payflow.payment.dto.PaymentResponse;
import com.payflow.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment initiation and lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/payments
    // ─────────────────────────────────────────────────────────────────
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Initiate a new payment",
        description = """
            Creates a new payment and submits it for fraud analysis.
            
            **Flow:**
            1. Validates the request
            2. Persists payment with status `PENDING`
            3. Publishes `payment.initiated` event to Kafka
            4. Updates status to `FRAUD_CHECK` and returns immediately
            
            The final status (`APPROVED` / `REJECTED`) is set asynchronously
            by the fraud-detection-service.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Payment initiated successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request payload",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class),
                examples = @ExampleObject(value = """
                    {
                      "type": "https://payflow.com/errors/validation-failed",
                      "title": "Bad Request",
                      "status": 400,
                      "detail": "amount: Amount must be greater than 0",
                      "timestamp": "2024-03-15T10:30:00Z"
                    }"""))),
        @ApiResponse(responseCode = "401", description = "Unauthorised — missing or invalid JWT"),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Payment initiation request",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = PaymentRequest.class),
                    examples = @ExampleObject(name = "EUR transfer", value = """
                        {
                          "senderId":    "user-abc-123",
                          "receiverId":  "user-xyz-456",
                          "amount":      "250.00",
                          "currency":    "EUR",
                          "description": "Invoice #INV-2024-001 payment"
                        }""")))
            PaymentRequest request) {

        log.info("REST request to initiate payment from {} amount {} {}",
            request.senderId(), request.amount(), request.currency());
        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/payments/{id}
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get payment by ID",
        description = "Retrieves the full details of a single payment by its UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment found",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<PaymentResponse> getPayment(
            @Parameter(description = "UUID of the payment", required = true,
                       example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        log.info("REST request to get payment: {}", id);
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/payments/sender/{senderId}
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/sender/{senderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Get all payments by sender",
        description = "Returns all payments initiated by the specified sender, ordered by creation date descending.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of payments (may be empty)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<List<PaymentResponse>> getPaymentsBySender(
            @Parameter(description = "Sender identifier", required = true, example = "user-abc-123")
            @PathVariable String senderId) {
        log.info("REST request to get payments for sender: {}", senderId);
        return ResponseEntity.ok(paymentService.getPaymentsBySender(senderId));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/payments/health
    // ─────────────────────────────────────────────────────────────────
    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
        summary     = "Service health check",
        description = "Lightweight liveness probe — returns HTTP 200 when the service is up.",
        security    = {})          // no auth required for health
    @ApiResponse(responseCode = "200", description = "Service is running",
        content = @Content(schema = @Schema(type = "string", example = "Payment Service is running")))
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Service is running");
    }
}
