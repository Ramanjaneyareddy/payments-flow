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

    private static final String PREFIX = "velocity:";
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public RuleResult evaluate(PaymentEvent event) {
        String senderId = event.senderId();

        try {
            long minCount = track(senderId, "min", Duration.ofMinutes(1));
            if (minCount > 2) {
                return RuleResult.flag(getRuleName(), 0.95, "Minute velocity limit exceeded: " + minCount);
            }

            long hourCount = track(senderId, "hour", Duration.ofHours(1));
            if (hourCount > 5) {
                return RuleResult.flag(getRuleName(), 0.7, "Hour velocity limit exceeded: " + hourCount);
            }
        } catch (Exception e) {
            log.warn("Velocity check bypassed (Redis error): {}", e.getMessage());
        }

        return RuleResult.pass(getRuleName());
    }

    private long track(String id, String suffix, Duration ttl) {
        String key = PREFIX + id + ":" + suffix;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, ttl);
        }
        return count != null ? count : 0;
    }

    @Override
    public String getRuleName() { return "VELOCITY_RULE"; }
}