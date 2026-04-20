package com.payflow.infrastructure.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(
                (consumerRecord, ex) -> log.error(
                        "Failed to process record topic={} offset={} payload={}",
                        consumerRecord.topic(), consumerRecord.offset(), consumerRecord.value(), ex),
                new FixedBackOff(0L, 0L)
        );
    }
}
