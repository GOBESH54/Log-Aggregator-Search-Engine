package com.logaggregator.service;

import com.logaggregator.config.LogProperties;
import com.logaggregator.model.LogEntry;
import com.logaggregator.model.LogSearchRequest;
import com.logaggregator.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {
    
    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    private final LogRepository logRepository;
    private final LogVectorStoreService logVectorStoreService;
    private final ApplicationEventPublisher events;
    private final LogProperties logProperties;

    public LogService(LogRepository logRepository,
                      LogVectorStoreService logVectorStoreService,
                      ApplicationEventPublisher events, LogProperties logProperties) {
        this.logRepository = logRepository;
        this.logVectorStoreService = logVectorStoreService;
        this.events = events;
        this.logProperties = logProperties;
    }

    @Transactional
    @CacheEvict(value = "searchResults", allEntries = true)
    public void ingestLogs(List<LogEntry> logs) {
        logs.forEach(log -> {
            if (log.getTimestamp() == null) {
                log.setTimestamp(LocalDateTime.now());
            }
        });
        List<LogEntry> savedLogs = logRepository.saveAll(logs);
        log.info("Ingested {} logs", savedLogs.size());

        if (logProperties.getAi().isEnabled()) {
            events.publishEvent(new LogsIngested(savedLogs));
        }
    }

    public record LogsIngested(List<LogEntry> entries) {}

    @Cacheable(value = "searchResults", key = "#request.toCacheKey()", condition = "#request.semanticSearch != true")
    public List<LogEntry> searchLogs(LogSearchRequest request) {
        validateTimeRange(request);

        String keyword = request.getSearchText();
        if (Boolean.TRUE.equals(request.getSemanticSearch())) {
            logProperties.getAi().requireEnabled();
            String query = resolveSemanticQuery(request);
            if (query == null || query.isBlank()) {
                throw new IllegalArgumentException("A query is required for semantic search");
            } else {
                List<LogEntry> semanticResults = logVectorStoreService.semanticSearch(query, request);
                if (!semanticResults.isEmpty()) {
                    return semanticResults;
                }
                log.info("Semantic search returned no results, falling back to keyword search");
                keyword = keyword == null || keyword.isBlank() ? query : keyword;
            }
        }

        LocalDateTime startTime = request.getStartTime() != null ?
            request.getStartTime() : LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = request.getEndTime() != null ?
            request.getEndTime() : LocalDateTime.now();

        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 100;
        long offset = (long) page * size;

        return logRepository.searchLogs(
            keyword,
            request.getSource(),
            request.getLevel(),
            startTime,
            endTime,
            size,
            offset
        );
    }

    public List<LogEntry> semanticSearchLogs(String query, LogSearchRequest request) {
        validateTimeRange(request);
        return logVectorStoreService.semanticSearch(query, request);
    }

    private String resolveSemanticQuery(LogSearchRequest request) {
        if (request.getSemanticQuery() != null && !request.getSemanticQuery().isBlank()) {
            return request.getSemanticQuery();
        }
        return request.getSearchText();
    }

    private void validateTimeRange(LogSearchRequest request) {
        if (request.getStartTime() != null
                && request.getEndTime() != null
                && request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }
}
