package br.com.freela.gateway.infrastructure.filter;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorrelationMdcFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String supplied = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        String correlationId = supplied == null || supplied.isBlank() ? UUID.randomUUID().toString() : supplied;
        String trace = exchange.getRequest().getHeaders().getFirst("traceparent");
        var request = exchange.getRequest().mutate().header("X-Correlation-Id", correlationId).build();
        return Mono.defer(() -> {
            try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", correlationId)) {
                if (trace != null) MDC.put("traceparent", trace);
                return chain.filter(exchange.mutate().request(request).build());
            }
        });
    }

    @Override
    public int getOrder() {
        return -99;
    }
}
