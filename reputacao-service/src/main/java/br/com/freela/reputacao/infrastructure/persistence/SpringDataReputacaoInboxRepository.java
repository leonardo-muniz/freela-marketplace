package br.com.freela.reputacao.infrastructure.persistence;

import br.com.freela.reputacao.domain.model.ReputacaoInbox;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataReputacaoInboxRepository extends JpaRepository<ReputacaoInbox, UUID> { }
