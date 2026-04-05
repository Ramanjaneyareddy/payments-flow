package com.payflow.payment.kafka;

import com.payflow.payment.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${payflow.kafka.topics.payment-initiated}")
    private String paymentInitiatedTopic;

    @Value("${payflow.kafka.topics.payment-completed}")
    private String paymentCompletedTopic;

    public void publishPaymentInitiated(PaymentEvent event) {
        publish(paymentInitiatedTopic, event);
    }

    public void publishPaymentCompleted(PaymentEvent event) {
        publish(paymentCompletedTopic, event);
    }

    private void publish(String topic, PaymentEvent event) {
        CompletableFuture<SendResult<String, PaymentEvent>> future =
            kafkaTemplate.send(topic, event.paymentId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event {} to topic {}: {}",
                    event.eventType(), topic, ex.getMessage());
            } else {
                log.info("Published event {} for payment {} to topic {} partition {} offset {}",
                    event.eventType(),
                    event.paymentId(),
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }
        });
    }
}
