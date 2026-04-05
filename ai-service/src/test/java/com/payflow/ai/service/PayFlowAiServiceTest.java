package com.payflow.ai.service;

import com.payflow.ai.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayFlowAiService Unit Tests")
class PayFlowAiServiceTest {

    @Mock private ChatClient.Builder chatClientBuilder;
    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock private ChatClient.CallResponseSpec callSpec;

    private PayFlowAiService aiService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.defaultSystem(any(String.class))).thenReturn(chatClientBuilder);
        //when(chatClientBuilder.defaultAdvisors(any())).thenReturn(chatClientBuilder);
        when(requestSpec.user(any(java.util.function.Consumer.class))).thenReturn(requestSpec);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        aiService = new PayFlowAiService(chatClientBuilder);
    }

    @Test
    @DisplayName("explainFraudDecision — REJECTED payment returns CRITICAL risk level")
    void explainFraudDecision_rejected_returnsCritical() {
        FraudExplainRequest req = new FraudExplainRequest(
            UUID.randomUUID(), "REJECTED", 1.0,
            List.of("BLACKLIST_RULE"),
            "Sender blocked-user-001 is on the static blacklist",
            new BigDecimal("100.00"), "EUR", "blocked-user-001"
        );

       // when(chatClient.prompt(any())).thenReturn(requestSpec);
        when(chatClient.prompt(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("""
            {
              "paymentId": "%s",
              "explanation": "Your payment was blocked for security reasons.",
              "customerGuidance": "Please contact support.",
              "complianceNote": "BLACKLIST_RULE triggered score=1.0.",
              "riskLevel": "CRITICAL"
            }""".formatted(req.paymentId()));

        FraudExplainResponse response = aiService.explainFraudDecision(req);

        assertThat(response).isNotNull();
        assertThat(response.riskLevel()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("assessPaymentRisk — high value returns REVIEW prediction")
    void assessPaymentRisk_highValue_returnsReview() {
        PaymentRiskRequest req = new PaymentRiskRequest(
            "user-abc", "user-xyz", new BigDecimal("15000.00"), "EUR", "Quarterly invoice");

        PaymentRiskResponse mockResponse = new PaymentRiskResponse(
            "HIGH", 0.55, "REVIEW",
            "Amount exceeds high-value threshold.",
            List.of("Amount > EUR10,000"),
            List.of("Split into smaller payments if possible")
        );

        when(chatClient.prompt(any((String.class)))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.entity(PaymentRiskResponse.class)).thenReturn(mockResponse);

        PaymentRiskResponse response = aiService.assessPaymentRisk(req);

        assertThat(response).isNotNull();
        assertThat(response.riskRating()).isEqualTo("HIGH");
        assertThat(response.predictedDecision()).isEqualTo("REVIEW");
        assertThat(response.predictedScore()).isGreaterThan(0.4);
    }

    @Test
    @DisplayName("chat — new session generates sessionId")
    void chat_newSession_returnsSessionId() {
        ChatRequest req = new ChatRequest("What is BLACKLIST_RULE?", null);

        when(chatClient.prompt()).thenReturn(requestSpec);
       // when(requestSpec.advisors(any((String.class)))).thenReturn(requestSpec);
        when(requestSpec.user(any(java.util.function.Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("BLACKLIST_RULE checks senders against the fraud watchlist.");

        ChatResponse response = aiService.chat(req);

        assertThat(response).isNotNull();
        assertThat(response.reply()).contains("BLACKLIST_RULE");
        assertThat(response.sessionId()).isNotBlank();
    }

    @Test
    @DisplayName("generateInsights — high rejection rate returns ALERT health")
    void generateInsights_highRejectionRate_returnsAlert() {
        InsightsRequest req = new InsightsRequest(
            100, 70, 20, 5, 5,
            new BigDecimal("500000"), new BigDecimal("5000"),
            new BigDecimal("50000"), new BigDecimal("10")
        );

        InsightsResponse mockResponse = new InsightsResponse(
            "ALERT", 20.0,
            List.of("Rejection rate 20% is 4x industry average"),
            List.of("Review AMOUNT_RULE thresholds"),
            "Portfolio shows elevated fraud activity."
        );

        when(chatClient.prompt(any((String.class)))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.entity(InsightsResponse.class)).thenReturn(mockResponse);

        InsightsResponse response = aiService.generateInsights(req);

        assertThat(response).isNotNull();
        assertThat(response.portfolioHealth()).isEqualTo("ALERT");
        assertThat(response.rejectionRatePercent()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("summariseSender — trusted sender returns TRUSTED profile")
    void summariseSender_trustedUser_returnsTrustedProfile() {
        SenderSummaryRequest req = new SenderSummaryRequest(
            "user-abc-123", 42, new BigDecimal("18500.00"),
            Map.of("COMPLETED", 40L, "REVIEW", 2L)
        );

        SenderSummaryResponse mockResponse = new SenderSummaryResponse(
            "user-abc-123", "TRUSTED",
            "User demonstrates consistent, low-risk payment behaviour.",
            List.of("Regular cadence", "No rejections"),
            List.of("Eligible for enhanced payment limits")
        );

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(java.util.function.Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.entity(SenderSummaryResponse.class)).thenReturn(mockResponse);

        SenderSummaryResponse response = aiService.summariseSender(req);

        assertThat(response).isNotNull();
        assertThat(response.senderId()).isEqualTo("user-abc-123");
        assertThat(response.riskProfile()).isEqualTo("TRUSTED");
    }
}
