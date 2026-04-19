package com.payflow.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.domain.model.audit.AuditLog;
import com.payflow.domain.model.event.ProcessedEvent;
import com.payflow.infrastructure.persistence.jpa.AuditLogRepository;
import com.payflow.infrastructure.persistence.jpa.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static com.payflow.infrastructure.kafka.TransactionOutboxWriter.*;

@Component
@RequiredArgsConstructor
@Log
public class AuditConsumer {
    private final TransactionTemplate transactionTemplate;
    private final ProcessedEventRepository processedEventRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private static final String CONSUMER_GROUP = "audit";
    @KafkaListener(
            topics = "${payflow.kafka.topics.transactions}",
            groupId = "${payflow.kafka.consumer.audit-group}"
    )
    public void handle(String payload, Acknowledgment ack ) {
        // insert into audit_logs table
        transactionTemplate.execute(
                status -> {
                    log.info("AuditConsumer received message: {}");

                    try {
                        TransactionCreatedPayload event = objectMapper
                                .readValue(payload,
                                        TransactionCreatedPayload.class);
                        // Check
                        if (processedEventRepository.existsByEventIdAndConsumerGroup(event.transactionId(),CONSUMER_GROUP)) {
                            return null;
                        }

                        // Process
                        auditLogRepository.save(AuditLog.builder()
                                .action(event.type().name())
                                .entityType("Transaction")
                                .entityId(event.transactionId())
                                .build());

                        // Ack
                        processedEventRepository.save(
                                ProcessedEvent.builder()
                                        .eventId(event.transactionId())
                                        .consumerGroup(CONSUMER_GROUP)
                                        .processedAt(Instant.now())
                                        .build()
                        );
                        ack.acknowledge();
                        return null;
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("Failed to deserialize payload", e);
                    }
                }
        );
    }
}
