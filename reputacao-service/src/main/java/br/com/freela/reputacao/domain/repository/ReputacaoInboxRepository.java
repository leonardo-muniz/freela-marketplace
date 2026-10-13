package br.com.freela.reputacao.domain.repository;

import br.com.freela.reputacao.domain.model.ReputacaoInbox;

import java.util.UUID;

public interface ReputacaoInboxRepository {
    boolean existsById(UUID eventId);
    ReputacaoInbox save(ReputacaoInbox inbox);
}
