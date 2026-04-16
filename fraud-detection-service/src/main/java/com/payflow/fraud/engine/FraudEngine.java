package com.payflow.fraud.engine;

import com.payflow.fraud.event.FraudResult;
import com.payflow.fraud.event.PaymentEvent;
import com.payflow.fraud.rule.FraudRule;
import com.payflow.fraud.rule.FraudRule.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudEngine {

    private static final double REJECT_THRESHOLD = 0.8;
    private static final double REVIEW_THRESHOLD = 0.4;

    private final List<FraudRule> rules;

    public FraudResult evaluate(PaymentEvent event) {
        log.info("Processing fraud check for payment: {}", event.paymentId());

        List<RuleResult> triggered = rules.stream()
                .map(rule -> rule.evaluate(event))
                .filter(RuleResult::triggered)
                .toList();

        double maxScore = triggered.stream()
                .mapToDouble(RuleResult::riskScore)
                .max()
                .orElse(0.0);

        FraudResult.FraudDecision decision = determineDecision(maxScore);

        return new FraudResult(
                event.paymentId(),
                decision,
                maxScore,
                triggered.stream().map(RuleResult::ruleName).toList(),
                triggered.stream().map(RuleResult::reason).findFirst().orElse("No risks detected"),
                Instant.now()
        );
    }

    private FraudResult.FraudDecision determineDecision(double score) {
        if (score >= REJECT_THRESHOLD) return FraudResult.FraudDecision.REJECTED;
        if (score >= REVIEW_THRESHOLD) return FraudResult.FraudDecision.REVIEW;
        return FraudResult.FraudDecision.APPROVED;
    }
}