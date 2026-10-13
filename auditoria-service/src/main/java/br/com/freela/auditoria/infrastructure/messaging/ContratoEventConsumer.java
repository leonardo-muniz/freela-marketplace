package br.com.freela.auditoria.infrastructure.messaging;

import br.com.freela.auditoria.domain.model.EventoAuditoria;
import br.com.freela.auditoria.domain.repository.EventoAuditoriaRepository;
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
    private final EventoAuditoriaRepository repository;

    public ContratoEventConsumer(ObjectMapper mapper, EventoAuditoriaRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @RetryableTopic(attempts = "4", dltTopicSuffix = ".dlt")
    @KafkaListener(topics = "contratos.v1", groupId = "auditoria-service")
    @Transactional
    public void consume(String raw) {
        try {
            JsonNode event = mapper.readTree(raw);
            UUID eventId = UUID.fromString(event.get("eventId").asText());
            if (repository.existsByEventId(eventId)) return;
            repository.save(new EventoAuditoria(
                    eventId,
                    UUID.fromString(event.get("aggregateId").asText()),
                    event.get("eventType").asText(),
                    event.get("correlationId").asText(null),
                    raw
            ));
            log.info("auditoria.processada eventId={} contratoId={}", eventId, event.get("aggregateId").asText());
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    @DltHandler
    public void dlt(String raw) {
        log.error("auditoria.dlt payload={}", raw);
    }
}
