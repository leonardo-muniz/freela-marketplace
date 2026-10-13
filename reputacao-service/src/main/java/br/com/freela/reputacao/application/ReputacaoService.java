package br.com.freela.reputacao.application;

import br.com.freela.reputacao.domain.model.ReputacaoFreelancer;
import br.com.freela.reputacao.domain.repository.ReputacaoRepository;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReputacaoService {

    private static final Logger log = LoggerFactory.getLogger(ReputacaoService.class);
    private final ReputacaoRepository repository;

    public ReputacaoService(ReputacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrarContratoConcluido(UUID contratoId, UUID freelancerId, BigDecimal valor) {
        log.info(
                "reputacao.atualizacao.inicio contratoId={} freelancerId={} valor={}",
                contratoId,
                freelancerId,
                valor
        );
        ReputacaoFreelancer reputacao = repository.findById(freelancerId)
                .orElseGet(() -> new ReputacaoFreelancer(freelancerId));
        reputacao.registrarContrato(valor);
        repository.save(reputacao);
        log.info(
                "reputacao.atualizacao.sucesso contratoId={} freelancerId={} contratosConcluidos={} valorTotal={}",
                contratoId,
                freelancerId,
                reputacao.getContratosConcluidos(),
                reputacao.getValorTotal()
        );
    }
}
