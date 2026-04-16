package com.payflow.fraud.kafka;

import com.payflow.fraud.engine.FraudEngine;
import com.payflow.fraud.event.FraudResult;
import com.payflow.fraud.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudEventConsumer {

    private final FraudEngine fraudEngine;
    private final KafkaTemplate<String, FraudResult> kafkaTemplate;

    @Value("${payflow.kafka.topics.fraud-result}")
    private String resultTopic;

    @KafkaListener(
            topics = "${payflow.kafka.topics.payment-initiated}",
            concurrency = "3"
    )
    public void onPaymentInitiated(PaymentEvent event) {
        log.info("Evaluating fraud for payment: {}", event.paymentId());

        try {
            FraudResult result = fraudEngine.evaluate(event);
            String key = event.paymentId().toString();

            kafkaTemplate.send(resultTopic, key, result)
                    .whenComplete((sr, ex) -> {
                        if (ex != null) {
                            log.error("Kafka publish error for ID {}: {}", key, ex.getMessage());
                        }
                    });

        } catch (Exception e) {
            log.error("Fraud processing failed for ID {}: {}", event.paymentId(), e.getMessage());
        }
    }
}