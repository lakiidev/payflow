package com.payflow.infrastructure.kafka;

import com.payflow.domain.model.outbox.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private  final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(OutboxEvent event) {
        kafkaTemplate.send(
                "transactions",
                event.getAggregateId().toString(),
                event.getPayload()
        );
    }
}
