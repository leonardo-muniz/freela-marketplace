package br.com.freela.reputacao.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reputacao_inbox")
public class ReputacaoInbox {

    @Id
    private UUID eventId;

    private Instant processedAt;

    protected ReputacaoInbox() { }

    public ReputacaoInbox(UUID eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }
}
