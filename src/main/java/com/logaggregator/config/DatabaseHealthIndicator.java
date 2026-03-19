package com.logaggregator.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("customDatabase")
public class DatabaseHealthIndicator implements ReactiveHealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mono<Health> health() {
        return Mono.fromCallable(() -> jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .map(result -> Health.up().withDetail("queryResult", result).build())
                .onErrorResume(ex -> Mono.just(Health.status(Status.DOWN)
                        .withDetail("error", ex.getMessage())
                        .build()));
    }
}
