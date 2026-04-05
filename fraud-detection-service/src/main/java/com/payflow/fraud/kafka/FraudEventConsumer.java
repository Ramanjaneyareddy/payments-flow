package com.payflow.fraud.kafka;

import com.payflow.fraud.engine.FraudEngine;
import com.payflow.fraud.event.FraudResult;
import com.payflow.fraud.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudEventConsumer {

    private final FraudEngine fraudEngine;
    private final KafkaTemplate<String, FraudResult> kafkaTemplate;

    // FIX #9: was hardcoded "fraud.result" — now injected from config
    @Value("${payflow.kafka.topics.fraud-result}")
    private String fraudResultTopic;

    @KafkaListener(
        topics      = "${payflow.kafka.topics.payment-initiated}",
        groupId     = "${spring.kafka.consumer.group-id}",
        concurrency = "3"
    )
    public void onPaymentInitiated(
            @Payload  PaymentEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET)             long offset) {

        log.info("Received payment event {} from partition {} offset {}",
            event.paymentId(), partition, offset);

        try {
            FraudResult result = fraudEngine.evaluate(event);

            kafkaTemplate.send(fraudResultTopic, event.paymentId().toString(), result)
                .whenComplete((sendResult, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish fraud result for payment {}: {}",
                            event.paymentId(), ex.getMessage());
                    } else {
                        log.info("Published fraud result {} for payment {} to topic {}",
                            result.decision(), event.paymentId(), fraudResultTopic);
                    }
                });

        } catch (Exception e) {
            log.error("Error processing fraud check for payment {}: {}",
                event.paymentId(), e.getMessage(), e);
            // TODO: route to Dead Letter Queue (DLQ) for retry / alerting
        }
    }
}
