package com.payflow.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payflow.BaseIntegrationTest;
import com.payflow.domain.model.audit.AuditLog;
import com.payflow.domain.model.transaction.TransactionType;
import com.payflow.infrastructure.kafka.TransactionOutboxWriter.TransactionCreatedPayload;
import com.payflow.infrastructure.persistence.jpa.AuditLogRepository;
import com.payflow.infrastructure.persistence.jpa.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
@Slf4j
class AuditConsumerIntegrationTest extends BaseIntegrationTest {

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;

    @Value("${payflow.kafka.topics.transactions}")
    private String transactionsTopic;

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Currency EUR = Currency.getInstance("EUR");


    @Test
    void shouldWriteAuditLogAndMarkEventProcessedWhenTransactionEventReceived() throws Exception {
        UUID eventId = UUID.randomUUID();

        kafkaTemplate.send(transactionsTopic, eventId.toString(), serialize(eventId)).get();

        await().atMost(15, SECONDS).untilAsserted(() -> {
            List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("Transaction", eventId);
            assertThat(logs).hasSize(1);
            assertThat(logs.getFirst().getAction()).isEqualTo("DEPOSIT");
            assertThat(logs.getFirst().getEntityId()).isEqualTo(eventId);
        });

        assertThat(processedEventRepository.existsByIdEventIdAndIdConsumerGroup(eventId, "audit")).isTrue();
    }

    @Test
    void shouldNotWriteDuplicateAuditLogWhenSameEventDeliveredTwice() throws Exception {
        UUID eventId = UUID.randomUUID();
        String payload = serialize(eventId);

        kafkaTemplate.send(transactionsTopic, eventId.toString(), payload).get();

        await().atMost(15, SECONDS).untilAsserted(() ->
                assertThat(auditLogRepository.findByEntityTypeAndEntityId("Transaction", eventId)).hasSize(1));

        kafkaTemplate.send(transactionsTopic, eventId.toString(), payload).get();

        // wait long enough for a reprocessing attempt, assert still only 1
        await().atMost(15, SECONDS).untilAsserted(() -> {
            assertThat(auditLogRepository.findByEntityTypeAndEntityId("Transaction", eventId)).hasSize(1);
            assertThat(processedEventRepository.existsByIdEventIdAndIdConsumerGroup(eventId, "audit")).isTrue();
        });
    }

    private static String serialize(UUID eventId) throws Exception {
        return MAPPER.writeValueAsString(new TransactionCreatedPayload(
                eventId,
                TransactionType.DEPOSIT,
                null,
                UUID.randomUUID(),
                5000L,
                EUR,
                Instant.now()
        ));
    }
}
