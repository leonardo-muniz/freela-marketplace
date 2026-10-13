package br.com.freela.notificacao.infrastructure.persistence;

import br.com.freela.notificacao.domain.model.Notificacao;
import br.com.freela.notificacao.domain.repository.NotificacaoRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class NotificacaoRepositoryJpaAdapter implements NotificacaoRepository {

    private final SpringDataNotificacaoRepository repository;

    public NotificacaoRepositoryJpaAdapter(SpringDataNotificacaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Notificacao> findAll() {
        return repository.findAll();
    }

    @Override
    public Notificacao save(Notificacao notificacao) {
        return repository.save(notificacao);
    }

    @Override
    public boolean existsByEventId(UUID eventId) {
        return repository.existsByEventId(eventId);
    }
}
