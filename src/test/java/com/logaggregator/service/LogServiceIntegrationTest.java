package com.logaggregator.service;

import com.logaggregator.model.LogEntry;
import com.logaggregator.model.LogSearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class LogServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg15")
            .withDatabaseName("logdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("APP_USERNAME", () -> "test-user");
        registry.add("APP_PASSWORD", () -> "test-pass");
        registry.add("JWT_SECRET", () -> "12345678901234567890123456789012");
        registry.add("spring.cache.type", () -> "simple");
        registry.add("log.ai.anomaly.cron", () -> "0 0 0 1 1 ? 2099");
    }

    @Autowired
    private LogService logService;

    @Test
    void shouldIngestAndSearchLogs() {
        LogEntry entry = new LogEntry();
        entry.setSource("integration-test");
        entry.setLevel("ERROR");
        entry.setMessage("Database timeout in integration test");
        entry.setTimestamp(LocalDateTime.now());

        logService.ingestLogs(List.of(entry));

        LogSearchRequest searchRequest = new LogSearchRequest();
        searchRequest.setSearchText("database");
        searchRequest.setSize(20);
        searchRequest.setPage(0);

        List<LogEntry> results = logService.searchLogs(searchRequest);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getMessage()).containsIgnoringCase("database");
    }
}
