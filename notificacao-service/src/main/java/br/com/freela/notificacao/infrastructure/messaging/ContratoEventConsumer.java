package br.com.freela.notificacao.infrastructure.messaging;

import br.com.freela.notificacao.domain.model.Notificacao;
import br.com.freela.notificacao.domain.repository.NotificacaoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ContratoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ContratoEventConsumer.class);
    private final ObjectMapper mapper;
    private final NotificacaoRepository repository;

    public ContratoEventConsumer(ObjectMapper mapper, NotificacaoRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @RetryableTopic(attempts = "4", dltTopicSuffix = ".dlt")
    @KafkaListener(topics = "contratos.v1", groupId = "notificacao-service")
    @Transactional
    public void consume(String raw) {
        try {
            JsonNode event = mapper.readTree(raw);
            UUID eventId = UUID.fromString(event.get("eventId").asText());
            if (repository.existsByEventId(eventId)) {
                log.info("kafka.idempotente eventId={}", eventId);
                return;
            }
            JsonNode payload = event.get("payload");
            UUID contratoId = UUID.fromString(event.get("contratoId").asText());
            UUID destinatarioId = payload.has("freelancerId") ? UUID.fromString(payload.get("freelancerId").asText()) : null;
            repository.save(new Notificacao(eventId, contratoId, destinatarioId, event.get("eventType").asText(), "Evento " + event.get("eventType").asText() + " recebido"));
            log.info("notificacao.processada eventId={} contratoId={} correlationId={}", eventId, contratoId, event.get("correlationId").asText(null));
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao processar evento", exception);
        }
    }

    @DltHandler
    public void dlt(String raw) {
        log.error("notificacao.dlt payload={}", raw);
    }
}
