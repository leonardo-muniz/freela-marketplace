package br.com.freela.contrato.infrastructure.messaging;

import br.com.freela.contrato.infrastructure.persistence.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxRepository repo;
    private final KafkaTemplate<String, String> kafka;

    public OutboxPublisher(OutboxRepository r, KafkaTemplate<String, String> k) {
        repo = r;
        kafka = k;
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.delay-ms:1000}")
    @Transactional
    public void publish() {
        for (var event : repo.findTop50ByStatusAndNextAttemptAtLessThanEqualOrderByOccurredAtAsc(
                "PENDING",
                Instant.now()
        ))
            try {
                kafka.send(MessageBuilder.withPayload(event.getPayload())
                        .setHeader(KafkaHeaders.TOPIC, event.getTopic())
                        .setHeader(KafkaHeaders.KEY, event.getAggregateId())
                        .setHeader("X-Correlation-Id", event.getCorrelationId())
                        .setHeader("traceparent", event.getTraceparent())
                        .build()
                ).get();
                event.published();
                log.info(
                        "outbox.publicado eventId={} contratoId={} attempts={}",
                        event.getEventId(),
                        event.getAggregateId(),
                        event.getAttempts()
                );
            } catch (Exception ex) {
                event.failed();
                log.error(
                        "outbox.falha eventId={} attempts={} status={}",
                        event.getEventId(),
                        event.getAttempts(),
                        event.getStatus(),
                        ex
                );
            }
    }
}
