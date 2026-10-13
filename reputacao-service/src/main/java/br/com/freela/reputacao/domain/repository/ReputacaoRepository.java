package br.com.freela.reputacao.domain.repository;

import br.com.freela.reputacao.domain.model.ReputacaoFreelancer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReputacaoRepository {
    List<ReputacaoFreelancer> findAll();
    Optional<ReputacaoFreelancer> findById(UUID freelancerId);
    ReputacaoFreelancer save(ReputacaoFreelancer reputacao);
}
