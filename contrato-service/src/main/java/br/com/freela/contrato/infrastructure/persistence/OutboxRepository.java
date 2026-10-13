package br.com.freela.contrato.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEventEntity, UUID> {
    List<OutboxEventEntity> findTop50ByStatusAndNextAttemptAtLessThanEqualOrderByOccurredAtAsc(String status, Instant now);
}
