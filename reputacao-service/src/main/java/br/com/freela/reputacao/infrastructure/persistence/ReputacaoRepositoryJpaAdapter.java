package br.com.freela.reputacao.infrastructure.persistence;

import br.com.freela.reputacao.domain.model.ReputacaoFreelancer;
import br.com.freela.reputacao.domain.repository.ReputacaoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class ReputacaoRepositoryJpaAdapter implements ReputacaoRepository {

    private final SpringDataReputacaoRepository repository;

    public ReputacaoRepositoryJpaAdapter(SpringDataReputacaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ReputacaoFreelancer> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<ReputacaoFreelancer> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public ReputacaoFreelancer save(ReputacaoFreelancer value) {
        return repository.save(value);
    }
}
