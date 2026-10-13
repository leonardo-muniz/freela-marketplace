package br.com.freela.notificacao.domain.repository;

import br.com.freela.notificacao.domain.model.Notificacao;

import java.util.List;
import java.util.UUID;

public interface NotificacaoRepository {
    List<Notificacao> findAll();
    Notificacao save(Notificacao notificacao);
    boolean existsByEventId(UUID eventId);
}
