package com.payflow.fraud.engine;

import com.payflow.fraud.event.FraudResult;
import com.payflow.fraud.event.PaymentEvent;
import com.payflow.fraud.rule.AmountRule;
import com.payflow.fraud.rule.BlacklistRule;
import com.payflow.fraud.rule.PatternRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FraudEngine Tests")
class FraudEngineTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private SetOperations<String, String> setOps;

    private FraudEngine fraudEngine;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOps);
        lenient().when(valueOps.increment(anyString())).thenReturn(1L);
        lenient(). when(setOps.isMember(anyString(), anyString())).thenReturn(false);

        var rules = List.of(
            new AmountRule(),
            new BlacklistRule(redisTemplate),
            new PatternRule()
        );
        fraudEngine = new FraudEngine(rules);
    }

    @Test
    @DisplayName("Should APPROVE normal low-value payment")
    void shouldApproveNormalPayment() {
        PaymentEvent event = normalPayment("100.00");
        FraudResult result = fraudEngine.evaluate(event);
        assertThat(result.decision()).isEqualTo(FraudResult.FraudDecision.APPROVED);
        assertThat(result.score()).isLessThan(0.4);
        assertThat(result.triggeredRules()).isEmpty();
    }

    @Test
    @DisplayName("Should REVIEW high value payment over €10,000")
    void shouldReviewHighValuePayment() {
        PaymentEvent event = normalPayment("15000.00");
        FraudResult result = fraudEngine.evaluate(event);
        assertThat(result.decision()).isEqualTo(FraudResult.FraudDecision.REVIEW);
        assertThat(result.triggeredRules()).contains("AMOUNT_RULE");
    }

    @Test
    @DisplayName("Should REJECT very high value payment over €50,000")
    void shouldRejectVeryHighValuePayment() {
        PaymentEvent event = normalPayment("75000.00");
        FraudResult result = fraudEngine.evaluate(event);
        assertThat(result.decision()).isEqualTo(FraudResult.FraudDecision.REJECTED);
        assertThat(result.triggeredRules()).contains("AMOUNT_RULE");
        assertThat(result.score()).isGreaterThanOrEqualTo(0.8);
    }

    private PaymentEvent normalPayment(String amount) {
        return paymentFrom("sender-001", amount);
    }

    private PaymentEvent paymentFrom(String senderId, String amount) {
        return new PaymentEvent(
            UUID.randomUUID(),
            senderId,
            "receiver-001",
            new BigDecimal(amount),
            "EUR",
            "PAYMENT_INITIATED",
            Instant.now()
        );
    }
}
