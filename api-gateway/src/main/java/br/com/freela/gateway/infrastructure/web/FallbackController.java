package br.com.freela.gateway.infrastructure.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {
    @RequestMapping("/fallback/contratos")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> contratosIndisponiveis() {
        return Mono.just(Map.of("code", "CONTRATO_SERVICE_UNAVAILABLE", "message", "O serviço de contratos está temporariamente indisponível"));
    }
}
