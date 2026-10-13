package br.com.freela.auditoria.infrastructure.persistence;

import br.com.freela.auditoria.domain.model.EventoAuditoria;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataEventoAuditoriaRepository extends JpaRepository<EventoAuditoria, UUID> {
    boolean existsByEventId(UUID eventId);
}
