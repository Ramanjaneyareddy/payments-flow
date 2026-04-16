package com.payflow.payment.controller;

import com.payflow.payment.dto.PaymentRequest;
import com.payflow.payment.dto.PaymentResponse;
import com.payflow.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initiate a new payment", description = "Asynchronously processes a new payment via Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment initiated"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public PaymentResponse initiatePayment(@Valid @RequestBody PaymentRequest request) {
        log.info("REST request to initiate payment from {} amount {} {}", request.senderId(), request.amount(), request.currency());
        return paymentService.initiatePayment(request);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get payment by ID", description = "Retrieves the full details of a single payment by its UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment found",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public PaymentResponse getPayment(
            @Parameter(description = "UUID of the payment", required = true,
                       example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        log.info("REST request to get payment: {}", id);
        return paymentService.getPaymentById(id);
    }

    @GetMapping(value = "/sender/{senderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all payments by sender", description = "Returns all payments initiated by the specified sender, ordered by creation date descending.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of payments (may be empty)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public List<PaymentResponse> getPaymentsBySender(
            @Parameter(description = "Sender identifier", required = true, example = "user-abc-123")
            @PathVariable String senderId) {
        log.info("REST request to get payments for sender: {}", senderId);
        return paymentService.getPaymentsBySender(senderId);
    }

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
