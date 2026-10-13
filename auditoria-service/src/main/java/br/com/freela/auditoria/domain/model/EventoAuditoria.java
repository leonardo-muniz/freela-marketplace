package br.com.freela.auditoria.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auditoria_eventos", uniqueConstraints = @UniqueConstraint(name = "uk_auditoria_event", columnNames = "eventId"))
public class EventoAuditoria {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    private UUID aggregateId;

    private String eventType;

    private String correlationId;

    @Column(columnDefinition = "text")
    private String payload;

    private Instant recebidoEm;

    protected EventoAuditoria() {
    }

    public EventoAuditoria(UUID eventId, UUID aggregateId, String eventType, String correlationId, String payload) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.correlationId = correlationId;
        this.payload = payload;
        this.recebidoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }
}
