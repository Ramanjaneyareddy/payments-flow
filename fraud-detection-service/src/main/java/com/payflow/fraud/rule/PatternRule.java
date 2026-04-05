package com.payflow.fraud.rule;

import com.payflow.fraud.event.PaymentEvent;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class PatternRule implements FraudRule {

    private static final int ODD_HOUR_START = 1; // 1 AM
    private static final int ODD_HOUR_END   = 5; // 5 AM

    @Override
    public RuleResult evaluate(PaymentEvent event) {
        ZonedDateTime eventTime = event.occurredAt()
            .atZone(ZoneId.of("Europe/Amsterdam"));

        int hour = eventTime.getHour();

        // Flag transactions between 1AM - 5AM Amsterdam time
        if (hour >= ODD_HOUR_START && hour <= ODD_HOUR_END) {
            return RuleResult.flag(getRuleName(), 0.4,
                "Transaction initiated at unusual hour: " + hour + ":00 Amsterdam time");
        }

        // Flag round-number large amounts (common in structuring fraud)
        var amount = event.amount();
        if (amount.scale() == 0 || amount.remainder(java.math.BigDecimal.valueOf(1000))
                .compareTo(java.math.BigDecimal.ZERO) == 0) {
            if (amount.compareTo(java.math.BigDecimal.valueOf(5000)) >= 0) {
                return RuleResult.flag(getRuleName(), 0.3,
                    "Suspiciously round large amount: €" + amount);
            }
        }

        return RuleResult.pass(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "PATTERN_RULE";
    }
}
