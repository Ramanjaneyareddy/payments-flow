package com.payflow.fraud.rule;

import com.payflow.fraud.event.PaymentEvent;

public interface FraudRule {

    /**
     * Evaluate the payment event and return a RuleResult.
     * Score: 0.0 = no risk, 1.0 = maximum risk
     */
    RuleResult evaluate(PaymentEvent event);

    String getRuleName();

    record RuleResult(
        String ruleName,
        boolean triggered,
        double riskScore,
        String reason
    ) {
        public static RuleResult pass(String ruleName) {
            return new RuleResult(ruleName, false, 0.0, null);
        }

        public static RuleResult flag(String ruleName, double score, String reason) {
            return new RuleResult(ruleName, true, score, reason);
        }
    }
}
