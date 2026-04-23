package com.payflow.domain.model.outbox;

public enum OutboxEventStatus {
    PENDING,     // not yet picked up
    PROCESSED,   // successfully published
    FAILED,      // exhausted retries
}
