package br.com.freela.reputacao.infrastructure.messaging;

import br.com.freela.reputacao.domain.model.ReputacaoFreelancer;
import br.com.freela.reputacao.domain.model.ReputacaoInbox;
import br.com.freela.reputacao.domain.repository.ReputacaoInboxRepository;
import br.com.freela.reputacao.domain.repository.ReputacaoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ContratoConcluidoConsumer {

    private static final Logger log = LoggerFactory.getLogger(ContratoConcluidoConsumer.class);
    private final ObjectMapper mapper;
    private final ReputacaoRepository reputacao;
    private final ReputacaoInboxRepository inbox;

    public ContratoConcluidoConsumer(ObjectMapper mapper, ReputacaoRepository reputacao, ReputacaoInboxRepository inbox) {
        this.mapper = mapper;
        this.reputacao = reputacao;
        this.inbox = inbox;
    }

    @RetryableTopic(attempts = "4", dltTopicSuffix = ".dlt")
    @KafkaListener(topics = "contratos.v1", groupId = "reputacao-service")
    @Transactional
    @CacheEvict(cacheNames = "reputacoes", allEntries = true)
    public void consume(String raw) {
        try {
            JsonNode event = mapper.readTree(raw);
            if (!"ContratoConcluido".equals(event.get("eventType").asText())) return;
            UUID eventId = UUID.fromString(event.get("eventId").asText());
            if (inbox.existsById(eventId)) return;
            JsonNode payload = event.get("payload");
            UUID freelancerId = UUID.fromString(payload.get("freelancerId").asText());
            ReputacaoFreelancer reputacaoAtual = reputacao.findById(freelancerId).orElseGet(() -> new ReputacaoFreelancer(freelancerId));
            reputacaoAtual.registrarContrato(new BigDecimal(payload.get("valor").asText()));
            reputacao.save(reputacaoAtual);
            inbox.save(new ReputacaoInbox(eventId));
            log.info("reputacao.processada eventId={} contratoId={}", eventId, event.get("contratoId").asText());
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    @DltHandler
    public void dlt(String raw) {
        log.error("reputacao.dlt payload={}", raw);
    }
}
