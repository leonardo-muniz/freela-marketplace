package br.com.freela.reputacao.infrastructure.persistence;

import br.com.freela.reputacao.domain.model.ReputacaoInbox;
import br.com.freela.reputacao.domain.repository.ReputacaoInboxRepository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class ReputacaoInboxRepositoryJpaAdapter implements ReputacaoInboxRepository {

    private final SpringDataReputacaoInboxRepository repository;

    public ReputacaoInboxRepositoryJpaAdapter(SpringDataReputacaoInboxRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public ReputacaoInbox save(ReputacaoInbox value) {
        return repository.save(value);
    }
}
