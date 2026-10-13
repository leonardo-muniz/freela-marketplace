package br.com.freela.reputacao.infrastructure.web;

import br.com.freela.reputacao.domain.model.ReputacaoFreelancer;
import br.com.freela.reputacao.domain.repository.ReputacaoRepository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reputacoes")
public class ReputacaoController {

    private static final Logger log = LoggerFactory.getLogger(ReputacaoController.class);
    private final ReputacaoRepository repository;

    public ReputacaoController(ReputacaoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Cacheable("reputacoes")
    public List<ReputacaoFreelancer> listar(@RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        log.info("http.reputacao.listar correlationId={}", correlationId);
        return repository.findAll();
    }
}
