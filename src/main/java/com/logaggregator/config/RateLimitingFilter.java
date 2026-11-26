package com.logaggregator.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements WebFilter {

    private final SecurityProperties securityProperties;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitingFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        if (!securityProperties.getRateLimit().isEnabled() || isPublicPath(exchange.getRequest().getPath().value())) {
            return chain.filter(exchange);
        }

        int limit = securityProperties.getRateLimit().getRequestsPerMinute();
        long currentWindowMinute = System.currentTimeMillis() / 60000;
        String clientKey = resolveClientKey(exchange);

        WindowCounter counter = counters.compute(clientKey, (key, existing) -> {
            if (existing == null || existing.windowMinute != currentWindowMinute) {
                return new WindowCounter(currentWindowMinute, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (counter != null && counter.count.get() > limit) {
            return tooManyRequests(exchange, limit);
        }

        cleanupOldCounters(currentWindowMinute);
        return chain.filter(exchange);
    }

    private String resolveClientKey(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        if (exchange.getRequest().getRemoteAddress() != null
                && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown-client";
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator/health")
                || "/actuator/info".equals(path)
                || "/actuator/prometheus".equals(path);
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange, int limit) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set("Retry-After", "60");
        byte[] body = ("{\"error\":\"TOO_MANY_REQUESTS\",\"message\":\"Rate limit exceeded: "
                + limit
                + " requests per minute\"}")
                .getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private void cleanupOldCounters(long currentWindowMinute) {
        if (counters.size() < 1000) {
            return;
        }
        counters.entrySet().removeIf(entry -> entry.getValue().windowMinute < currentWindowMinute - 2);
    }

    private static class WindowCounter {
        private final long windowMinute;
        private final AtomicInteger count;

        private WindowCounter(long windowMinute, AtomicInteger count) {
            this.windowMinute = windowMinute;
            this.count = count;
        }
    }
}
