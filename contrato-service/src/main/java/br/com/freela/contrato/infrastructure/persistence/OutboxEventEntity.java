package br.com.freela.contrato.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", indexes = @Index(name = "idx_outbox_pending", columnList = "status,nextAttemptAt"))
public class OutboxEventEntity {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false, length = 120)
    private String topic;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private Instant occurredAt;
    private Instant nextAttemptAt;

    @Column(length = 120)
    private String correlationId;

    @Column(length = 255)
    private String traceparent;

    protected OutboxEventEntity() { }

    public OutboxEventEntity(UUID id, String type, String aggregate, String topic, String payload, Instant occurred, String correlation, String trace) {
        eventId = id;
        eventType = type;
        aggregateId = aggregate;
        this.topic = topic;
        this.payload = payload;
        occurredAt = occurred;
        status = "PENDING";
        attempts = 0;
        nextAttemptAt = Instant.now();
        correlationId = correlation;
        traceparent = trace;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getTopic() {
        return topic;
    }

    public String getPayload() {
        return payload;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getTraceparent() {
        return traceparent;
    }

    public void published() {
        status = "PUBLISHED";
    }

    public void failed() {
        attempts++;
        status = attempts >= 5 ? "DEAD" : "PENDING";
        nextAttemptAt = Instant.now().plusSeconds(Math.min(300, attempts * 10L));
    }
}
