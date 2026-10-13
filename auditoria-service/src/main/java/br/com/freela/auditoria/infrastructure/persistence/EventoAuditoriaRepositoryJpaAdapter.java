package br.com.freela.auditoria.infrastructure.persistence;

import br.com.freela.auditoria.domain.model.EventoAuditoria;
import br.com.freela.auditoria.domain.repository.EventoAuditoriaRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class EventoAuditoriaRepositoryJpaAdapter implements EventoAuditoriaRepository {

    private final SpringDataEventoAuditoriaRepository repository;

    public EventoAuditoriaRepositoryJpaAdapter(SpringDataEventoAuditoriaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<EventoAuditoria> findAll() {
        return repository.findAll();
    }

    @Override
    public EventoAuditoria save(EventoAuditoria value) {
        return repository.save(value);
    }

    @Override
    public boolean existsByEventId(UUID id) {
        return repository.existsByEventId(id);
    }
}
