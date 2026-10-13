package br.com.freela.contrato.application;

import java.math.BigDecimal;
import java.util.UUID;

public record CriarContratoCommand(UUID clienteId, UUID freelancerId, String titulo, BigDecimal valor,
                                   String correlationId, String traceparent) {
    public CriarContratoCommand(UUID c, UUID f, String t, BigDecimal v) {
        this(c, f, t, v, null, null);
    }
}
