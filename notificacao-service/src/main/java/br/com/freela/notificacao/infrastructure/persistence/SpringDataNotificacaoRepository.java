package br.com.freela.notificacao.infrastructure.persistence;

import br.com.freela.notificacao.domain.model.Notificacao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataNotificacaoRepository extends JpaRepository<Notificacao, UUID> {
    boolean existsByEventId(UUID eventId);
}
