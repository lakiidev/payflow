package com.payflow.infrastructure.kafka.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyticsConsumer {

    @KafkaListener(
            topics = "${payflow.kafka.topics.transactions}",
            groupId = "${payflow.kafka.consumer.analytics-group}"
    )
    public void handle(String payload) {
        // write to TimescaleDB / analytics store
    }
}
