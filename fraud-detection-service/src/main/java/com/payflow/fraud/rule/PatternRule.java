package com.payflow.fraud.rule;

import com.payflow.fraud.event.PaymentEvent;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.ZoneId;

@Component
public class PatternRule implements FraudRule {

    private static final ZoneId TZ_AMS = ZoneId.of("Europe/Amsterdam");
    private static final BigDecimal ROUND_FACTOR = BigDecimal.valueOf(1000);
    private static final BigDecimal LARGE_ROUND_THRESHOLD = BigDecimal.valueOf(5000);

    @Override
    public RuleResult evaluate(PaymentEvent event) {
        // 1. Time-based anomaly (1 AM - 5 AM)
        int hour = event.occurredAt().atZone(TZ_AMS).getHour();
        if (hour >= 1 && hour <= 5) {
            return RuleResult.flag(getRuleName(), 0.4, "Off-hours transaction: " + hour + ":00 AMS");
        }

        // 2. Large round-number check
        BigDecimal amount = event.amount();
        boolean isRound = amount.remainder(ROUND_FACTOR).compareTo(BigDecimal.ZERO) == 0;

        if (isRound && amount.compareTo(LARGE_ROUND_THRESHOLD) >= 0) {
            return RuleResult.flag(getRuleName(), 0.3, "Large round-number pattern: " + amount);
        }

        return RuleResult.pass(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "PATTERN_RULE";
    }
}