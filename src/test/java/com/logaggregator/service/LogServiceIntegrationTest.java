package com.logaggregator.service;

import com.logaggregator.dto.AuthTokenResponse;
import com.logaggregator.model.LogEntry;
import com.logaggregator.model.LogSearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers(disabledWithoutDocker = true)
class LogServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg15").asCompatibleSubstituteFor("postgres"))
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
        registry.add("log.ai.enabled", () -> false);
    }

    @Autowired
    private LogService logService;

    @Autowired
    private WebTestClient webClient;

    @Test
    void shouldAuthenticateAndUseTheHttpApi() {
        webClient.post().uri("/api/logs/search")
                .contentType(MediaType.APPLICATION_JSON).bodyValue("{}")
                .exchange().expectStatus().isUnauthorized();

        webClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"test-user\",\"password\":\"wrong\"}")
                .exchange().expectStatus().isUnauthorized();

        AuthTokenResponse response = webClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"test-user\",\"password\":\"test-pass\"}")
                .exchange().expectStatus().isOk()
                .expectBody(AuthTokenResponse.class).returnResult().getResponseBody();
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        WebTestClient authenticated = webClient.mutate()
                .defaultHeader("Authorization", "Bearer " + response.getToken()).build();

        authenticated.post().uri("/api/logs/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[{\"source\":\"http-test\",\"level\":\"INFO\",\"message\":\"Deployment smoke check\"}]")
                .exchange().expectStatus().isOk();

        authenticated.post().uri("/api/logs/search")
                .contentType(MediaType.APPLICATION_JSON).bodyValue("{\"source\":\"http-test\"}")
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].message").isEqualTo("Deployment smoke check");

        authenticated.post().uri("/api/logs/search/semantic")
                .contentType(MediaType.APPLICATION_JSON).bodyValue("{\"semanticQuery\":\"smoke\"}")
                .exchange().expectStatus().isEqualTo(503)
                .expectBody().jsonPath("$.message").value(message ->
                        assertThat(message.toString()).contains("AI features are disabled"));

        authenticated.post().uri("/api/logs/analyze/stream")
                .accept(MediaType.TEXT_EVENT_STREAM, MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).bodyValue("{}")
                .exchange().expectStatus().isEqualTo(503)
                .expectBody().jsonPath("$.message").value(message ->
                        assertThat(message.toString()).contains("AI features are disabled"));
    }

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
