package com.payflow.infrastructure.kafka.consumer;

import com.payflow.domain.model.audit.AuditLog;
import com.payflow.domain.model.transaction.TransactionType;
import com.payflow.infrastructure.BaseTransactionTest;
import com.payflow.infrastructure.kafka.TransactionOutboxWriter.TransactionCreatedPayload;
import com.payflow.infrastructure.persistence.jpa.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
class AuditConsumerIntegrationTest extends BaseTransactionTest {

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;
    @Autowired private WalletJpaRepository walletRepository;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private OutboxJpaRepository outboxRepository;
    @Value("${payflow.kafka.topics.transactions}")
    private String transactionsTopic;

    private static final Currency EUR = Currency.getInstance("EUR");


    private UUID userId;
    private UUID walletId;

    @BeforeEach
    void setUp() {
        userId = user.getId();
        walletId = wallet.getId();
    }

    @AfterEach
    void tearDown() {
        auditLogRepository.deleteAll();
    }

    @Test
    void shouldWriteAuditLogAndMarkEventProcessedWhenTransactionEventReceived() throws Exception {
        // Given
        UUID eventId = UUID.randomUUID();

        // When
        kafkaTemplate.send(transactionsTopic, eventId.toString(), serialize(eventId)).get();

        // Then
        await().atMost(15, SECONDS).untilAsserted(() -> {
            List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("Transaction", eventId);
            assertThat(logs).hasSize(1);
            assertThat(logs.getFirst().getAction()).isEqualTo("DEPOSIT");
            assertThat(logs.getFirst().getEntityId()).isEqualTo(eventId);
            assertThat(logs.getFirst().getUserId()).isNotNull();
        });

        assertThat(processedEventRepository.existsByIdEventIdAndIdConsumerGroup(eventId, "audit")).isTrue();
    }

    @Test
    void shouldNotWriteDuplicateAuditLogWhenSameEventDeliveredTwice() throws Exception {
        // Given
        UUID eventId = UUID.randomUUID();
        String payload = serialize(eventId);

        // When
        kafkaTemplate.send(transactionsTopic, eventId.toString(), payload).get();

        await().atMost(15, SECONDS).untilAsserted(() ->
                assertThat(auditLogRepository.findByEntityTypeAndEntityId("Transaction", eventId)).hasSize(1));

        kafkaTemplate.send(transactionsTopic, eventId.toString(), payload).get();

        // Then
        await().atMost(15, SECONDS).untilAsserted(() -> {
            assertThat(auditLogRepository.findByEntityTypeAndEntityId("Transaction", eventId)).hasSize(1);
            assertThat(processedEventRepository.existsByIdEventIdAndIdConsumerGroup(eventId, "audit")).isTrue();
        });
    }

    private String serialize(UUID eventId) {
        return mapper.writeValueAsString(new TransactionCreatedPayload(
                eventId,
                userId,      // real user ID from DB
                TransactionType.DEPOSIT,
                null,
                walletId,    // real wallet ID from DB
                5000L,
                EUR,
                Instant.now()
        ));
    }
}