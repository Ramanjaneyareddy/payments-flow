package com.payflow.fraud.rule;

import com.payflow.fraud.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class VelocityRule implements FraudRule {

    private static final int MAX_TRANSACTIONS_PER_HOUR = 5;
    private static final int MAX_TRANSACTIONS_PER_MINUTE = 2;
    private static final String KEY_PREFIX = "velocity:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public RuleResult evaluate(PaymentEvent event) {
        String hourKey   = KEY_PREFIX + event.senderId() + ":hour";
        String minuteKey = KEY_PREFIX + event.senderId() + ":minute";

        try {
            Long hourCount   = increment(hourKey,   Duration.ofHours(1));
            Long minuteCount = increment(minuteKey, Duration.ofMinutes(1));

            if (minuteCount != null && minuteCount > MAX_TRANSACTIONS_PER_MINUTE) {
                return RuleResult.flag(getRuleName(), 0.95,
                    "Sender " + event.senderId() + " sent " + minuteCount +
                    " transactions in 1 minute (max " + MAX_TRANSACTIONS_PER_MINUTE + ")");
            }

            if (hourCount != null && hourCount > MAX_TRANSACTIONS_PER_HOUR) {
                return RuleResult.flag(getRuleName(), 0.7,
                    "Sender " + event.senderId() + " sent " + hourCount +
                    " transactions in 1 hour (max " + MAX_TRANSACTIONS_PER_HOUR + ")");
            }

        } catch (Exception e) {
            log.warn("Redis unavailable for velocity check, allowing payment: {}", e.getMessage());
        }

        return RuleResult.pass(getRuleName());
    }

    private Long increment(String key, Duration ttl) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, ttl);
        }
        return count;
    }

    @Override
    public String getRuleName() {
        return "VELOCITY_RULE";
    }
}
