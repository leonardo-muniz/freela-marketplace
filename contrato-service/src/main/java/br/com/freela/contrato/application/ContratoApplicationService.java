package br.com.freela.contrato.application;

import br.com.freela.contrato.domain.model.Contrato;
import br.com.freela.contrato.domain.repository.ContratoRepository;
import br.com.freela.contrato.infrastructure.messaging.EventEnvelope;
import br.com.freela.contrato.infrastructure.persistence.OutboxEventEntity;
import br.com.freela.contrato.infrastructure.persistence.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ContratoApplicationService {
 
    private static final Logger log = LoggerFactory.getLogger(ContratoApplicationService.class);
    private final ContratoRepository repository;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;

    public ContratoApplicationService(ContratoRepository r, OutboxRepository o, ObjectMapper m) {
        repository = r;
        outbox = o;
        mapper = m;
    }

    @Transactional
    public Contrato criar(CriarContratoCommand cmd) {
        Contrato contrato = Contrato.criar(
                cmd.clienteId(),
                cmd.freelancerId(),
                cmd.titulo(),
                cmd.valor()
        );
        Contrato saved = repository.salvar(contrato);
        registrarEventos(contrato, cmd.correlationId(), cmd.traceparent());
        log.info("contrato.criacao.sucesso contratoId={}", saved.id());
        return saved;
    }

    @Transactional
    public Contrato concluir(UUID id, String correlationId, String traceparent) {
        Contrato contrato = buscar(id);
        contrato.concluir();
        Contrato salvo = repository.salvar(contrato);
        registrarEventos(contrato, correlationId, traceparent);
        log.info("contrato.conclusao.sucesso contratoId={}", salvo.id());
        return salvo;
    }

    private void registrarEventos(Contrato contrato, String correlationId, String traceparent) {
        for (var event : contrato.pullDomainEvents())
            try {
                var envelope = new EventEnvelope(
                        event.eventId(),
                        event.eventType(),
                        1,
                        event.occurredAt(),
                        contrato.id(),
                        contrato.id(),
                        correlationId,
                        traceparent,
                        event
                );
                outbox.save(new OutboxEventEntity(
                        event.eventId(),
                        event.eventType(),
                        contrato.id().toString(),
                        "contratos.v1",
                        mapper.writeValueAsString(envelope),
                        event.occurredAt(),
                        correlationId,
                        traceparent
                ));
            } catch (Exception ex) {
                throw new IllegalStateException("Não foi possível registrar outbox", ex);
            }
    }

    @Transactional(readOnly = true)
    public Contrato buscar(UUID id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Contrato> listar() {
        return repository.listar();
    }
}
