package com.payflow.infrastructure.kafka;

import com.payflow.domain.model.outbox.OutboxEvent;
import com.payflow.domain.model.outbox.OutboxEventStatus;
import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.model.transaction.TransactionType;
import com.payflow.infrastructure.persistence.jpa.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publishTransactionCreated(Transaction tx)
    {
        OutboxEvent event =  OutboxEvent.builder()
                .aggregateId(tx.getId())
                .aggregateType(tx.getClass().getSimpleName())
                .eventType("TransactionCreated")
                .payload(serialize(toPayload(tx)))
                .status(OutboxEventStatus.PENDING)
                .build();
        outboxRepository.save(event);
    }

    private TransactionCreatedPayload toPayload(Transaction tx)
    {
        return new TransactionCreatedPayload(
                tx.getId(),
                tx.getType(),
                tx.getFromWalletId(),
                tx.getToWalletId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getCompletedAt()
        );
    }

    public String serialize(TransactionCreatedPayload payload)
    {
        try {
            return objectMapper.writeValueAsString(payload);
        }
        catch (JacksonException e)
        {
            throw new IllegalStateException("Failed to serialize outbox payload: " + payload.getClass().getSimpleName(), e);
        }
    }

    public record TransactionCreatedPayload(
            UUID transactionId,
            TransactionType type,
            UUID fromWalletId,
            UUID toWalletId,
            long amountCents,
            Currency currency,
            Instant completedAt
    ) {}
}
