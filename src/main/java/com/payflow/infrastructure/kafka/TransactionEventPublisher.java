package com.payflow.infrastructure.kafka;

import com.payflow.domain.model.outbox.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${payflow.kafka.topics.transactions}")
    private String transactionsTopic;

    public void publish(OutboxEvent event) {
        kafkaTemplate.send(transactionsTopic, event.getAggregateId().toString(), event.getPayload())
                .join();
    }
}
