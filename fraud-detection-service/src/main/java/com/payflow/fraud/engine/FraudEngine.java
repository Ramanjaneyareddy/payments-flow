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
        log.info("Evaluating fraud for payment {} amount {} {}",
            event.paymentId(), event.amount(), event.currency());

        // Run all rules
        List<RuleResult> results = rules.stream()
            .map(rule -> rule.evaluate(event))
            .toList();

        // Collect triggered rules
        List<String> triggeredRules = results.stream()
            .filter(RuleResult::triggered)
            .map(RuleResult::ruleName)
            .toList();

        // Aggregate score — take maximum risk score across all rules
        double aggregatedScore = results.stream()
            .mapToDouble(RuleResult::riskScore)
            .max()
            .orElse(0.0);

        // Build reason from triggered rules
        String reason = results.stream()
            .filter(RuleResult::triggered)
            .map(RuleResult::reason)
            .findFirst()
            .orElse(null);

        // Make decision
        FraudResult.FraudDecision decision = determineDecision(aggregatedScore);

        log.info("Fraud evaluation complete for payment {}: score={}, decision={}, triggeredRules={}",
            event.paymentId(), aggregatedScore, decision, triggeredRules);

        return new FraudResult(
            event.paymentId(),
            decision,
            aggregatedScore,
            triggeredRules,
            reason,
            Instant.now()
        );
    }

    private FraudResult.FraudDecision determineDecision(double score) {
        if (score >= REJECT_THRESHOLD) return FraudResult.FraudDecision.REJECTED;
        if (score >= REVIEW_THRESHOLD) return FraudResult.FraudDecision.REVIEW;
        return FraudResult.FraudDecision.APPROVED;
    }
}
