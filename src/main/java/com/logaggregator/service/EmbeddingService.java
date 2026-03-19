package com.logaggregator.service;

import com.logaggregator.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final LogVectorStoreService logVectorStoreService;

    public EmbeddingService(LogVectorStoreService logVectorStoreService) {
        this.logVectorStoreService = logVectorStoreService;
    }

    @Async("embeddingExecutor")
    public CompletableFuture<Void> indexLogsAsync(List<LogEntry> logs) {
        try {
            logVectorStoreService.indexLogs(logs);
        } catch (Exception ex) {
            log.error("Log ingestion succeeded but embedding indexing failed", ex);
        }
        return CompletableFuture.completedFuture(null);
    }
}
