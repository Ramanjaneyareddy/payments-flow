package com.payflow.fraud.rule;

import com.payflow.fraud.event.PaymentEvent;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class AmountRule implements FraudRule {

    private static final BigDecimal HIGH_VALUE_THRESHOLD    = new BigDecimal("10000.00");
    private static final BigDecimal VERY_HIGH_VALUE_THRESHOLD = new BigDecimal("50000.00");

    @Override
    public RuleResult evaluate(PaymentEvent event) {
        BigDecimal amount = event.amount();

        if (amount.compareTo(VERY_HIGH_VALUE_THRESHOLD) >= 0) {
            return RuleResult.flag(getRuleName(), 0.9,
                "Transaction amount €" + amount + " exceeds very high value threshold");
        }

        if (amount.compareTo(HIGH_VALUE_THRESHOLD) >= 0) {
            return RuleResult.flag(getRuleName(), 0.5,
                "Transaction amount €" + amount + " exceeds high value threshold");
        }

        return RuleResult.pass(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "AMOUNT_RULE";
    }
}
