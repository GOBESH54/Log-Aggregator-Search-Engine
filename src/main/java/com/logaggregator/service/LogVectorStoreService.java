package com.logaggregator.service;

import com.logaggregator.model.LogEntry;
import com.logaggregator.model.LogSearchRequest;
import com.logaggregator.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LogVectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(LogVectorStoreService.class);
    private static final String META_LOG_ID = "logId";
    private static final String META_SOURCE = "source";
    private static final String META_LEVEL = "level";
    private static final String META_TIMESTAMP = "timestamp";

    private final VectorStore vectorStore;
    private final LogRepository logRepository;

    public LogVectorStoreService(VectorStore vectorStore, LogRepository logRepository) {
        this.vectorStore = vectorStore;
        this.logRepository = logRepository;
    }

    public void indexLogs(List<LogEntry> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }

        List<Document> documents = logs.stream()
                .filter(logEntry -> logEntry.getId() != null)
                .map(this::toDocument)
                .collect(Collectors.toList());

        if (documents.isEmpty()) {
            return;
        }

        vectorStore.add(documents);
        log.info("Indexed {} logs into pgvector store", documents.size());
    }

    public List<LogEntry> semanticSearch(String query, LogSearchRequest request) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        int topK = resolveSemanticTopK(request);
        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThresholdAll();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderedIds = documents.stream()
                .map(this::extractLogId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (orderedIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(orderedIds);
        Map<Long, LogEntry> logById = new HashMap<>();
        for (LogEntry logEntry : logRepository.findByIdIn(new ArrayList<>(uniqueIds))) {
            logById.put(logEntry.getId(), logEntry);
        }

        List<LogEntry> orderedLogs = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Long id : orderedIds) {
            if (!seen.add(id)) {
                continue;
            }
            LogEntry logEntry = logById.get(id);
            if (logEntry != null && matchesFilters(logEntry, request)) {
                orderedLogs.add(logEntry);
            }
        }

        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 100;
        int offset = page * size;

        if (offset >= orderedLogs.size()) {
            return Collections.emptyList();
        }

        int endIndex = Math.min(offset + size, orderedLogs.size());
        return orderedLogs.subList(offset, endIndex);
    }

    private Document toDocument(LogEntry logEntry) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(META_LOG_ID, logEntry.getId());
        metadata.put(META_SOURCE, logEntry.getSource());
        metadata.put(META_LEVEL, logEntry.getLevel());
        metadata.put(META_TIMESTAMP, logEntry.getTimestamp() != null ? logEntry.getTimestamp().toString() : null);

        String content = String.format(
                "source=%s level=%s timestamp=%s message=%s metadata=%s",
                safe(logEntry.getSource()),
                safe(logEntry.getLevel()),
                logEntry.getTimestamp(),
                safe(logEntry.getMessage()),
                safe(logEntry.getMetadata()));

        return new Document("log-" + logEntry.getId(), content, metadata);
    }

    private Long extractLogId(Document document) {
        if (document == null || document.getMetadata() == null) {
            return null;
        }

        Object idValue = document.getMetadata().get(META_LOG_ID);
        if (idValue instanceof Number number) {
            return number.longValue();
        }
        if (idValue instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean matchesFilters(LogEntry logEntry, LogSearchRequest request) {
        LocalDateTime startTime = request.getStartTime() != null
                ? request.getStartTime()
                : LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = request.getEndTime() != null
                ? request.getEndTime()
                : LocalDateTime.now();

        boolean sourceMatches = request.getSource() == null || request.getSource().isBlank()
                || request.getSource().equals(logEntry.getSource());
        boolean levelMatches = request.getLevel() == null || request.getLevel().isBlank()
                || request.getLevel().equalsIgnoreCase(logEntry.getLevel());
        boolean timestampMatches = logEntry.getTimestamp() != null
                && !logEntry.getTimestamp().isBefore(startTime)
                && !logEntry.getTimestamp().isAfter(endTime);

        return sourceMatches && levelMatches && timestampMatches;
    }

    private int resolveSemanticTopK(LogSearchRequest request) {
        if (request.getSemanticTopK() != null && request.getSemanticTopK() > 0) {
            return request.getSemanticTopK();
        }
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 100;
        return Math.max(size, 50);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
