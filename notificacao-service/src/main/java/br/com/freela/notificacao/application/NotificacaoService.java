package br.com.freela.notificacao.application;

import br.com.freela.notificacao.domain.model.Notificacao;
import br.com.freela.notificacao.domain.repository.NotificacaoRepository;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);
    private final NotificacaoRepository repository;

    public NotificacaoService(NotificacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrar(UUID contratoId, UUID destinatarioId, String tipo, String mensagem) {
        log.info("notificacao.registro.inicio contratoId={} destinatarioId={} tipo={}", contratoId, destinatarioId, tipo);
        Notificacao notificacao = repository.save(new Notificacao(contratoId, destinatarioId, tipo, mensagem));
        log.info("notificacao.registro.sucesso notificacaoId={} contratoId={} destinatarioId={}", notificacao.getId(), contratoId, destinatarioId);
    }
}
