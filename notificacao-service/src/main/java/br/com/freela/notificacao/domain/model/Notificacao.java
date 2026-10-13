package br.com.freela.notificacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notificacoes", uniqueConstraints = @UniqueConstraint(name = "uk_notificacao_event", columnNames = "eventId"))
public class Notificacao {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    private UUID contratoId;
    private UUID destinatarioId;
    private String tipo;

    @Column(length = 500)
    private String mensagem;

    private Instant criadaEm;

    protected Notificacao() { }

    public Notificacao(UUID eventId, UUID contratoId, UUID destinatarioId, String tipo, String mensagem) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.contratoId = contratoId;
        this.destinatarioId = destinatarioId;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.criadaEm = Instant.now();
    }

    public Notificacao(UUID contratoId, UUID destinatarioId, String tipo, String mensagem) {
        this(UUID.randomUUID(), contratoId, destinatarioId, tipo, mensagem);
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }
}
