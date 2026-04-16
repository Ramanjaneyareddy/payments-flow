package com.payflow.fraud.rule;

import com.payflow.fraud.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlacklistRule implements FraudRule {

    private static final String REDIS_KEY = "fraud:blacklist";
    private static final Set<String> STATIC_LIST = Set.of("blocked-001", "blocked-002");

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public RuleResult evaluate(PaymentEvent event) {
        if (isBlacklisted(event.senderId())) {
            return RuleResult.flag(getRuleName(), 1.0, "Sender is blacklisted: " + event.senderId());
        }

        if (isBlacklisted(event.receiverId())) {
            return RuleResult.flag(getRuleName(), 1.0, "Receiver is blacklisted: " + event.receiverId());
        }

        return RuleResult.pass(getRuleName());
    }

    private boolean isBlacklisted(String id) {
        if (id == null) return false;
        if (STATIC_LIST.contains(id)) return true;

        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(REDIS_KEY, id));
        } catch (Exception e) {
            log.warn("Redis unavailable for blacklist check on ID: {}", id);
            return false; // Fail open
        }
    }

    @Override
    public String getRuleName() { return "BLACKLIST_RULE"; }
}