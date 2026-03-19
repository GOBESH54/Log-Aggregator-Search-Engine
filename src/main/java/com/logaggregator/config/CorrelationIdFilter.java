package com.logaggregator.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        final String finalCorrelationId = correlationId;
        return chain.filter(exchange)
                .doFirst(() -> MDC.put(CORRELATION_ID_MDC_KEY, finalCorrelationId))
                .doFinally(signalType -> MDC.remove(CORRELATION_ID_MDC_KEY));
    }
}
