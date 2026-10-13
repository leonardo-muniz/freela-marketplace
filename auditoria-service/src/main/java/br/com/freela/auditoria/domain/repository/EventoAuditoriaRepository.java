package br.com.freela.auditoria.domain.repository;

import br.com.freela.auditoria.domain.model.EventoAuditoria;

import java.util.List;
import java.util.UUID;

public interface EventoAuditoriaRepository {
    List<EventoAuditoria> findAll();
    EventoAuditoria save(EventoAuditoria evento);
    boolean existsByEventId(UUID eventId);
}
