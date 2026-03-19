package com.logaggregator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component("ollama")
public class OllamaHealthIndicator implements ReactiveHealthIndicator {

    private final WebClient webClient;

    public OllamaHealthIndicator(WebClient.Builder builder,
                                 @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String ollamaBaseUrl) {
        this.webClient = builder
                .baseUrl(ollamaBaseUrl)
                .build();
    }

    @Override
    public Mono<Health> health() {
        return webClient.get()
                .uri("/api/tags")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return Health.up().withDetail("status", response.getStatusCode().value()).build();
                    }
                    return Health.status(Status.DOWN)
                            .withDetail("status", response.getStatusCode().value())
                            .build();
                })
                .onErrorResume(ex -> Mono.just(Health.status(Status.DOWN)
                        .withDetail("error", ex.getMessage())
                        .build()));
    }
}
