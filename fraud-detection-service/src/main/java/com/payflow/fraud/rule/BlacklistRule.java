package com.payflow.fraud.rule;

import com.payflow.fraud.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BlacklistRule implements FraudRule {

    private static final String BLACKLIST_KEY = "fraud:blacklist";

    // Hardcoded for demo — in production load from DB/config
    private static final Set<String> STATIC_BLACKLIST = Set.of(
        "blocked-user-001", "blocked-user-002", "sanctioned-entity-003"
    );

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public RuleResult evaluate(PaymentEvent event) {
        String senderId = event.senderId();

        // Check static blacklist
        if (STATIC_BLACKLIST.contains(senderId)) {
            return RuleResult.flag(getRuleName(), 1.0,
                "Sender " + senderId + " is on the static blacklist");
        }

        // Check dynamic Redis blacklist
        try {
            Boolean isBlacklisted = redisTemplate.opsForSet().isMember(BLACKLIST_KEY, senderId);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                return RuleResult.flag(getRuleName(), 1.0,
                    "Sender " + senderId + " is on the dynamic blacklist");
            }
        } catch (Exception e) {
            // Redis unavailable — fail open (allow), log warning
        }

        // Check receiver blacklist
        String receiverId = event.receiverId();
        if (STATIC_BLACKLIST.contains(receiverId)) {
            return RuleResult.flag(getRuleName(), 1.0,
                "Receiver " + receiverId + " is on the blacklist");
        }

        return RuleResult.pass(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "BLACKLIST_RULE";
    }
}
