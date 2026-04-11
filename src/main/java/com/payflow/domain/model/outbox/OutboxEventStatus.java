package com.payflow.domain.model.outbox;

public enum OutboxEventStatus {
    PENDING,     // not yet picked up
    PROCESSING,  // relay picked it up, publishing in progress
    PROCESSED,   // successfully published
    FAILED,      // exhausted retries
}
