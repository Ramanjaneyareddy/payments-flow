package com.payflow.ai.service;

import com.payflow.ai.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PayFlowAiService — Spring AI 1.0.0-M6 patterns demonstrated:
 *
 *  1. ChatClient fluent API with defaultSystem prompt
 *  2. PromptTemplate + variable injection via HashMap
 *  3. BeanOutputConverter for typed structured JSON output
 *  4. .entity(Class) for automatic structured output parsing
 *  5. MessageChatMemoryAdvisor for stateful multi-turn conversation
 *  6. SimpleLoggerAdvisor for request/response observability
 *  7. .stream().content() returning Flux<String> for SSE streaming
 */
@Service
public class PayFlowAiService {

    private static final Logger log = LoggerFactory.getLogger(PayFlowAiService.class);

    private final ChatClient chatClient;

    // Per-session conversation memory keyed by sessionId
    // In production: replace with Redis-backed ChatMemory for horizontal scaling
    private final Map<String, InMemoryChatMemory> chatMemories = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
        You are PayFlow AI, an expert financial analyst and fraud investigator
        embedded in the PayFlow cloud-native payments platform.

        Your role:
        - Explain fraud decisions clearly to both customers and compliance officers
        - Assess payment risk using industry best practices (PSD2, AML, FATF guidelines)
        - Provide actionable, empathetic guidance
        - Always be factual, concise, and professional

        Platform context:
        - PayFlow processes payments in EUR, USD, and other currencies
        - Fraud rules: AMOUNT_RULE (>EUR10K=0.5 risk, >EUR50K=0.9),
          VELOCITY_RULE (>2/min=0.95, >5/hr=0.7),
          BLACKLIST_RULE (score=1.0),
          PATTERN_RULE (odd hours/round amounts=0.3-0.4)
        - Decision thresholds: APPROVED <0.4, REVIEW 0.4-0.8, REJECTED >=0.8
        - Never reveal internal security thresholds or blacklist contents to customers

        Response style:
        - Plain English for customers; technical precision for compliance
        - Keep responses concise and structured
        """;

    public PayFlowAiService(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem(SYSTEM_PROMPT)
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .build();
    }

    // ── 1. Fraud Explain — PromptTemplate + BeanOutputConverter ─────────────
    public FraudExplainResponse explainFraudDecision(FraudExplainRequest req) {
        log.info("Explaining fraud decision for payment {} decision={} score={}",
            req.paymentId(), req.decision(), req.score());

        var converter = new BeanOutputConverter<>(FraudExplainResponse.class);

        Map<String, Object> vars = new HashMap<>();
        vars.put("paymentId",     req.paymentId().toString());
        vars.put("senderId",      req.senderId() != null ? req.senderId() : "unknown");
        vars.put("amount",        req.amount() != null ? req.amount().toPlainString() : "unknown");
        vars.put("currency",      req.currency() != null ? req.currency() : "EUR");
        vars.put("decision",      req.decision());
        vars.put("score",         String.format("%.2f", req.score()));
        vars.put("triggeredRules", req.triggeredRules() != null ? req.triggeredRules().toString() : "[]");
        vars.put("rawReason",     req.rawReason() != null ? req.rawReason() : "not specified");
        vars.put("format",        converter.getFormat());

        var prompt = new PromptTemplate("""
            Analyse this fraud decision and provide a structured explanation.

            Payment ID:      {paymentId}
            Sender:          {senderId}
            Amount:          {amount} {currency}
            Fraud Decision:  {decision}
            Risk Score:      {score}
            Triggered Rules: {triggeredRules}
            Engine Reason:   {rawReason}

            Provide:
            1. explanation — 2-3 sentence customer-facing explanation (empathetic, no jargon)
            2. customerGuidance — what should the customer do next
            3. complianceNote — brief technical note for compliance team
            4. riskLevel — one of: LOW, MEDIUM, HIGH, CRITICAL

            {format}
            """).create(vars);

        String raw = chatClient.prompt(prompt).call().content();
        FraudExplainResponse response = converter.convert(raw);
        log.info("Explanation generated for payment {} riskLevel={}",
            req.paymentId(), response != null ? response.riskLevel() : "null");
        return response;
    }

    // ── 2. Risk Assessment — .entity() structured output ────────────────────
    public PaymentRiskResponse assessPaymentRisk(PaymentRiskRequest req) {
        log.info("Assessing risk: sender={} amount={} {}",
            req.senderId(), req.amount(), req.currency());

        Map<String, Object> vars = new HashMap<>();
        vars.put("senderId",    req.senderId());
        vars.put("receiverId",  req.receiverId());
        vars.put("amount",      req.amount().toPlainString());
        vars.put("currency",    req.currency());
        vars.put("description", req.description() != null ? req.description() : "not provided");

        var prompt = new PromptTemplate("""
            Perform a pre-submission risk assessment for this proposed payment.

            Sender:      {senderId}
            Receiver:    {receiverId}
            Amount:      {amount} {currency}
            Description: {description}

            Analyse against PayFlow fraud rules and return:
            - riskRating: LOW / MEDIUM / HIGH / CRITICAL
            - predictedScore: estimated fraud score 0.0-1.0
            - predictedDecision: APPROVED / REVIEW / REJECTED
            - narrative: 2-3 sentence explanation
            - riskFactors: list of risk factors (empty list if none)
            - recommendations: list of suggestions (empty list if none)
            """).create(vars);

        return chatClient.prompt(prompt).call().entity(PaymentRiskResponse.class);
    }

    // ── 3. Multi-turn Chat — MessageChatMemoryAdvisor ────────────────────────
    public ChatResponse chat(ChatRequest req) {
        String sessionId = req.sessionId() != null ? req.sessionId() : UUID.randomUUID().toString();
        log.info("Chat request session={}", sessionId);

        InMemoryChatMemory memory = chatMemories.computeIfAbsent(
            sessionId, id -> new InMemoryChatMemory());

        String reply = chatClient
            .prompt()
            .advisors(new MessageChatMemoryAdvisor(memory))
            .user(req.message())
            .call()
            .content();

        return new ChatResponse(reply, sessionId);
    }

    // ── 4. Streaming Chat — Flux<String> SSE ────────────────────────────────
    public Flux<String> streamChat(String message, String sessionId) {
        String sid = sessionId != null ? sessionId : UUID.randomUUID().toString();
        InMemoryChatMemory memory = chatMemories.computeIfAbsent(
            sid, id -> new InMemoryChatMemory());

        return chatClient
            .prompt()
            .advisors(new MessageChatMemoryAdvisor(memory))
            .user(message)
            .stream()
            .content();
    }

    // ── 5. Portfolio Insights — PromptTemplate + .entity() ───────────────────
    public InsightsResponse generateInsights(InsightsRequest req) {
        log.info("Generating portfolio insights: total={} rejected={}",
            req.totalPayments(), req.rejectedPayments());

        double rejRate = req.totalPayments() > 0
            ? (double) req.rejectedPayments() / req.totalPayments() * 100 : 0;
        double revRate = req.totalPayments() > 0
            ? (double) req.reviewPayments() / req.totalPayments() * 100 : 0;
        double appRate = req.totalPayments() > 0
            ? (double) req.approvedPayments() / req.totalPayments() * 100 : 0;

        Map<String, Object> vars = new HashMap<>();
        vars.put("total",        String.valueOf(req.totalPayments()));
        vars.put("approved",     String.valueOf(req.approvedPayments()));
        vars.put("rejected",     String.valueOf(req.rejectedPayments()));
        vars.put("review",       String.valueOf(req.reviewPayments()));
        vars.put("pending",      String.valueOf(req.pendingPayments()));
        vars.put("approvalRate", String.format("%.1f", appRate));
        vars.put("rejRate",      String.format("%.1f", rejRate));
        vars.put("reviewRate",   String.format("%.1f", revRate));
        vars.put("totalAmount",  req.totalAmount()   != null ? req.totalAmount().toPlainString()   : "0");
        vars.put("avgAmount",    req.averageAmount()  != null ? req.averageAmount().toPlainString() : "0");
        vars.put("maxAmount",    req.maxAmount()      != null ? req.maxAmount().toPlainString()     : "0");
        vars.put("minAmount",    req.minAmount()      != null ? req.minAmount().toPlainString()     : "0");

        var prompt = new PromptTemplate("""
            Analyse these payment portfolio statistics for a compliance team.

            Total payments:   {total}
            Approved:         {approved} ({approvalRate}%)
            Rejected:         {rejected} ({rejRate}%)
            Under review:     {review} ({reviewRate}%)
            Pending:          {pending}
            Total volume:     EUR {totalAmount}
            Average amount:   EUR {avgAmount}
            Max payment:      EUR {maxAmount}
            Min payment:      EUR {minAmount}

            Industry benchmark: rejection rate 1-3% normal, >5% concern, >10% alert.

            Return:
            - portfolioHealth: HEALTHY / ATTENTION / ALERT / CRITICAL
            - rejectionRatePercent: actual rate as decimal number
            - keyFindings: 3-5 specific data-backed findings
            - recommendations: 3-5 actionable items
            - executiveSummary: 2-3 sentence executive summary
            """).create(vars);

        return chatClient.prompt(prompt).call().entity(InsightsResponse.class);
    }

    // ── 6. Sender Profile — .user(lambda).param() ───────────────────────────
    public SenderSummaryResponse summariseSender(SenderSummaryRequest req) {
        log.info("Summarising sender: {} total={}", req.senderId(), req.totalPayments());

        String breakdown = req.statusBreakdown() != null
            ? req.statusBreakdown().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .reduce((a, b) -> a + ", " + b).orElse("n/a")
            : "n/a";

        return chatClient
            .prompt()
            .user(u -> u.text("""
                Generate a payment behaviour profile for sender {senderId}.

                Total payments:   {total}
                Completed amount: EUR {amount}
                Status breakdown: {breakdown}

                Return:
                - senderId: the sender ID
                - riskProfile: TRUSTED / STANDARD / ELEVATED / HIGH_RISK
                - behaviourSummary: 2-3 sentence behavioural narrative
                - patterns: 2-4 notable patterns observed
                - recommendations: suggested actions for account management
                """)
                .param("senderId",  req.senderId())
                .param("total",     String.valueOf(req.totalPayments()))
                .param("amount",    req.totalAmount() != null ? req.totalAmount().toPlainString() : "0")
                .param("breakdown", breakdown))
            .call()
            .entity(SenderSummaryResponse.class);
    }
}
