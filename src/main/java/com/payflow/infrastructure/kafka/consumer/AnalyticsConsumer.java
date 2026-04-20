package com.payflow.infrastructure.kafka.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyticsConsumer {
    private static final String CONSUMER_GROUP = "analytics";

    @KafkaListener(
            topics = "${payflow.kafka.topics.transactions}",
            groupId = "${payflow.kafka.consumer.analytics-group}"
    )
    public void handle(String payload, Acknowledgment ack) {
        // write to TimescaleDB / analytics store
    }
}
