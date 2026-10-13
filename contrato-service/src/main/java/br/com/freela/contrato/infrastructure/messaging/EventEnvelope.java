package br.com.freela.contrato.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(UUID eventId, String eventType, int eventVersion, Instant occurredAt, UUID aggregateId,
                            UUID contratoId, String correlationId, String traceparent, Object payload) {
}
