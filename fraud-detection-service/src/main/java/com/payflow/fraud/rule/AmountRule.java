package com.payflow.fraud.rule;

import com.payflow.fraud.event.PaymentEvent;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class AmountRule implements FraudRule {

    private static final BigDecimal THRESHOLD_HIGH = BigDecimal.valueOf(10000);
    private static final BigDecimal THRESHOLD_CRITICAL = BigDecimal.valueOf(50000);

    @Override
    public RuleResult evaluate(PaymentEvent event) {
        BigDecimal amount = event.amount();

        if (amount.compareTo(THRESHOLD_CRITICAL) >= 0) {
            return RuleResult.flag(getRuleName(), 0.9, "Very high value transaction: " + amount);
        }

        if (amount.compareTo(THRESHOLD_HIGH) >= 0) {
            return RuleResult.flag(getRuleName(), 0.5, "High value transaction: " + amount);
        }

        return RuleResult.pass(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "AMOUNT_RULE";
    }
}