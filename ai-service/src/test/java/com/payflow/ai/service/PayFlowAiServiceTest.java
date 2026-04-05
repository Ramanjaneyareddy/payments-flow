package com.payflow.ai.service;

import com.payflow.ai.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayFlowAiService Unit Tests")
class PayFlowAiServiceTest {

    private ChatClient chatClient;
    private PayFlowAiService aiService;

    @BeforeEach
    void setUp() {
        // RETURNS_DEEP_STUBS allows chatClient.prompt().user().call().entity() to work in one go
        chatClient = mock(ChatClient.class, Answers.RETURNS_DEEP_STUBS);
        ChatClient.Builder mockBuilder = mock(ChatClient.Builder.class, Answers.RETURNS_SELF);
        when(mockBuilder.build()).thenReturn(chatClient);

        aiService = new PayFlowAiService(mockBuilder);
    }

   /* @Test
    @DisplayName("explainFraudDecision — returns CRITICAL risk level")
    void explainFraudDecision_rejected_returnsCritical() {
        // Match the 5-argument constructor: id, reason, decision, suggestedAction, riskLevel
        FraudExplainResponse mockResponse = new FraudExplainResponse(
                UUID.randomUUID(), "High risk pattern", "REJECTED", "Manual Review", "CRITICAL"
        );

        // Explicitly cast any() to Prompt to avoid ambiguity
        when(chatClient.prompt((Prompt) any())
                .call()
                .entity(eq(FraudExplainResponse.class)))
                .thenReturn(mockResponse);

        // Match the 8-argument constructor for FraudExplainRequest
        FraudExplainRequest req = new FraudExplainRequest(
                UUID.randomUUID(), "pay-123", 1.0, List.of("RULE_1"), "reason",
                new BigDecimal("100"), "USD", "metadata"
        );

        var result = aiService.explainFraudDecision(req);

        assertNotNull(result);
        assertEquals("CRITICAL", result.riskLevel()); // Use riskLevel() per DTO definition
    }*/

    /*@Test
    @DisplayName("assessPaymentRisk — high value returns REVIEW prediction")
    void assessPaymentRisk_highValue_returnsReview() {
        PaymentRiskRequest req = new PaymentRiskRequest(
                "u1", "u2", new BigDecimal("15000.00"), "EUR", "Invoice");

        PaymentRiskResponse mockResponse = new PaymentRiskResponse(
                "HIGH", 0.85, "REVIEW", "High amount", List.of("Limit"), List.of("Verify"));

        // Use anyString() to target the prompt(String) method signature
        when(chatClient.prompt(anyString())
                .call()
                .entity(eq(PaymentRiskResponse.class)))
                .thenReturn(mockResponse);

        PaymentRiskResponse response = aiService.assessPaymentRisk(req);

        assertNotNull(response);
        assertThat(response.riskRating()).isEqualTo("HIGH");
    }*/

    @Test
    @DisplayName("chat — returns expected reply")
    void chat_returnsExpectedReply() {
        ChatRequest req = new ChatRequest("Explain rules", "sess-123");

        // Mocking the advisor and user chain
        when(chatClient.prompt()
                .advisors((org.springframework.ai.chat.client.advisor.api.Advisor[]) any())
                .user(anyString())
                .call()
                .content())
                .thenReturn("Rules explained.");

        ChatResponse response = aiService.chat(req);

        assertThat(response).isNotNull();
        assertThat(response.reply()).isEqualTo("Rules explained.");
    }

    @Test
    @DisplayName("summariseSender — trusted sender returns TRUSTED profile")
    void summariseSender_trustedUser_returnsTrustedProfile() {
        SenderSummaryRequest req = new SenderSummaryRequest(
                "user-1", 10, new BigDecimal("500.00"), Map.of("OK", 10L));

        SenderSummaryResponse mockResponse = new SenderSummaryResponse(
                "user-1", "TRUSTED", "Clean history", List.of("Verified"), List.of("None"));

        // Use any(Consumer.class) for methods that take functional interfaces
        when(chatClient.prompt()
                .user(any(Consumer.class))
                .call()
                .entity(eq(SenderSummaryResponse.class)))
                .thenReturn(mockResponse);

        SenderSummaryResponse response = aiService.summariseSender(req);

        assertNotNull(response);
        assertThat(response.riskProfile()).isEqualTo("TRUSTED");
    }
}