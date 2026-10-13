package br.com.freela.reputacao.infrastructure.persistence;

import br.com.freela.reputacao.domain.model.ReputacaoFreelancer;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataReputacaoRepository extends JpaRepository<ReputacaoFreelancer, UUID> { }
