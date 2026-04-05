package com.payflow.ai.controller;

import com.payflow.ai.dto.*;
import com.payflow.ai.service.PayFlowAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "PayFlow AI",
     description = "AI-powered payment intelligence — Spring AI 1.0.0-M6 + OpenAI GPT-4o-mini")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final PayFlowAiService aiService;

    public AiController(PayFlowAiService aiService) {
        this.aiService = aiService;
    }

    // ── POST /api/v1/ai/fraud/explain ─────────────────────────────────────────
    @PostMapping(value = "/fraud/explain",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Explain a fraud decision",
        description = """
            Generates a natural-language explanation of a fraud engine decision.

            **Spring AI pattern:** `PromptTemplate` + `BeanOutputConverter` for structured JSON output.

            Returns a customer-facing explanation, actionable guidance, compliance note, and risk level.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Explanation generated",
            content = @Content(schema = @Schema(implementation = FraudExplainResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorised"),
        @ApiResponse(responseCode = "503", description = "OpenAI API unavailable")
    })
    public ResponseEntity<FraudExplainResponse> explainFraud(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                content = @Content(schema = @Schema(implementation = FraudExplainRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "paymentId":      "550e8400-e29b-41d4-a716-446655440000",
                          "senderId":       "blocked-user-001",
                          "amount":         100.00,
                          "currency":       "EUR",
                          "decision":       "REJECTED",
                          "score":          1.0,
                          "triggeredRules": ["BLACKLIST_RULE"],
                          "rawReason":      "Sender blocked-user-001 is on the static blacklist"
                        }""")))
            FraudExplainRequest request) {
        log.info("POST /fraud/explain payment={}", request.paymentId());
        return ResponseEntity.ok(aiService.explainFraudDecision(request));
    }

    // ── POST /api/v1/ai/payment/assess-risk ──────────────────────────────────
    @PostMapping(value = "/payment/assess-risk",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Assess payment risk before submission",
        description = """
            Pre-submission risk prediction for a proposed payment.

            **Spring AI pattern:** `.entity(PaymentRiskResponse.class)` — automatic structured output.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Risk assessment completed",
            content = @Content(schema = @Schema(implementation = PaymentRiskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<PaymentRiskResponse> assessRisk(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                content = @Content(schema = @Schema(implementation = PaymentRiskRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "senderId":    "user-abc-123",
                          "receiverId":  "user-xyz-456",
                          "amount":      15000.00,
                          "currency":    "EUR",
                          "description": "Quarterly supplier invoice Q1-2024"
                        }""")))
            PaymentRiskRequest request) {
        log.info("POST /payment/assess-risk sender={} amount={} {}",
            request.senderId(), request.amount(), request.currency());
        return ResponseEntity.ok(aiService.assessPaymentRisk(request));
    }

    // ── POST /api/v1/ai/chat ──────────────────────────────────────────────────
    @PostMapping(value = "/chat",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Chat with PayFlow AI assistant",
        description = """
            Multi-turn conversational assistant with full PayFlow context.

            **Spring AI pattern:** `MessageChatMemoryAdvisor` with `InMemoryChatMemory`
            for per-session stateful conversation.

            Pass the same `sessionId` across requests to maintain conversation history.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "AI response",
            content = @Content(schema = @Schema(implementation = ChatResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                content = @Content(schema = @Schema(implementation = ChatRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "message":   "Why was my payment rejected?",
                          "sessionId": "session-abc-123"
                        }""")))
            ChatRequest request) {
        log.info("POST /chat session={}", request.sessionId());
        return ResponseEntity.ok(aiService.chat(request));
    }

    // ── GET /api/v1/ai/chat/stream ────────────────────────────────────────────
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary     = "Stream a chat response (Server-Sent Events)",
        description = """
            Streams the AI response token-by-token via SSE.

            **Spring AI pattern:** `.stream().content()` returns `Flux<String>`.

            ```bash
            curl -N "http://localhost:8087/api/v1/ai/chat/stream?message=Explain+BLACKLIST_RULE"
            ```
            """)
    @ApiResponse(responseCode = "200", description = "SSE stream of tokens",
        content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
            schema = @Schema(type = "string")))
    public Flux<String> streamChat(
            @Parameter(description = "Chat message", required = true,
                       example = "Explain what BLACKLIST_RULE means")
            @RequestParam String message,
            @Parameter(description = "Session ID", example = "session-abc-123")
            @RequestParam(required = false) String sessionId) {
        log.info("GET /chat/stream session={}", sessionId);
        return aiService.streamChat(message, sessionId);
    }

    // ── POST /api/v1/ai/insights ──────────────────────────────────────────────
    @PostMapping(value = "/insights",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Generate portfolio-level payment insights",
        description = """
            Analyses portfolio statistics and generates AI-driven insights and anomaly detection.

            **Spring AI pattern:** `PromptTemplate` with `HashMap` variables + `.entity()` output.

            Feed the output of `GET /api/v1/inquiry/stats` directly into this endpoint.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Insights generated",
            content = @Content(schema = @Schema(implementation = InsightsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<InsightsResponse> generateInsights(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                content = @Content(schema = @Schema(implementation = InsightsRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "totalPayments":    1024,
                          "approvedPayments": 850,
                          "rejectedPayments": 95,
                          "pendingPayments":  42,
                          "reviewPayments":   37,
                          "totalAmount":      4875000.00,
                          "averageAmount":    4760.74,
                          "maxAmount":        75000.00,
                          "minAmount":        0.50
                        }""")))
            InsightsRequest request) {
        log.info("POST /insights total={} rejected={}",
            request.totalPayments(), request.rejectedPayments());
        return ResponseEntity.ok(aiService.generateInsights(request));
    }

    // ── POST /api/v1/ai/sender/summarise ─────────────────────────────────────
    @PostMapping(value = "/sender/summarise",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary     = "Summarise a sender's payment behaviour",
        description = """
            Generates a plain-English behavioural profile for a sender.

            **Spring AI pattern:** `.user(u -> u.text(...).param(...))` — lambda-based prompt.

            Feed the output of `GET /api/v1/inquiry/stats/sender/{senderId}` into this endpoint.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Summary generated",
            content = @Content(schema = @Schema(implementation = SenderSummaryResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorised")
    })
    public ResponseEntity<SenderSummaryResponse> summariseSender(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                content = @Content(schema = @Schema(implementation = SenderSummaryRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "senderId":       "user-abc-123",
                          "totalPayments":  42,
                          "totalAmount":    18500.00,
                          "statusBreakdown": {
                            "COMPLETED": 35,
                            "REJECTED":   5,
                            "REVIEW":     2
                          }
                        }""")))
            SenderSummaryRequest request) {
        log.info("POST /sender/summarise sender={}", request.senderId());
        return ResponseEntity.ok(aiService.summariseSender(request));
    }

    // ── GET /api/v1/ai/health ─────────────────────────────────────────────────
    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "AI Service health check", security = {})
    @ApiResponse(responseCode = "200",
        content = @Content(schema = @Schema(type = "string",
            example = "AI Service is running | Model: gpt-4o-mini | Spring AI 1.0.0-M6")))
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Service is running | Model: gpt-4o-mini | Spring AI 1.0.0-M6");
    }
}
