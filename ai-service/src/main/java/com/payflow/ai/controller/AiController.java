package com.payflow.ai.controller;

import com.payflow.ai.dto.*;
import com.payflow.ai.service.PayFlowAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PayFlow AI", description = "AI-powered payment intelligence")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final PayFlowAiService aiService;

    @PostMapping("/fraud/explain")
    @Operation(summary = "Explain a fraud decision", description = "Generates natural-language reasoning for a rejected payment.")
    public ResponseEntity<FraudExplainResponse> explainFraud(@Valid @RequestBody FraudExplainRequest request) {
        log.info("Generating fraud explanation for: {}", request.paymentId());
        return ResponseEntity.ok(aiService.explainFraudDecision(request));
    }

    @PostMapping("/payment/assess-risk")
    @Operation(summary = "Pre-submission risk assessment", description = "Predicts risk profile for a proposed payment.")
    public ResponseEntity<PaymentRiskResponse> assessRisk(@Valid @RequestBody PaymentRiskRequest request) {
        log.info("Assessing risk for sender: {}", request.senderId());
        return ResponseEntity.ok(aiService.assessPaymentRisk(request));
    }

    @PostMapping("/chat")
    @Operation(summary = "Contextual AI assistant", description = "Maintains stateful conversation history using sessionId.")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(aiService.chat(request));
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Streamed AI assistant", description = "Token-by-token response via Server-Sent Events (SSE).")
    public Flux<String> streamChat(@RequestParam String message, @RequestParam(required = false) String sessionId) {
        return aiService.streamChat(message, sessionId);
    }

    @PostMapping("/insights")
    @Operation(summary = "Portfolio insights", description = "Anomaly detection and trends based on aggregate stats.")
    public ResponseEntity<InsightsResponse> generateInsights(@Valid @RequestBody InsightsRequest request) {
        return ResponseEntity.ok(aiService.generateInsights(request));
    }

    @PostMapping("/sender/summarise")
    @Operation(summary = "Sender behavior summary", description = "Plain-English profile of a sender's history.")
    public ResponseEntity<SenderSummaryResponse> summariseSender(@Valid @RequestBody SenderSummaryRequest request) {
        return ResponseEntity.ok(aiService.summariseSender(request));
    }

    @GetMapping("/health")
    public String health() {
        return "AI Service is running";
    }
}