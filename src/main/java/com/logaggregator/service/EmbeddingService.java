package com.logaggregator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final LogVectorStoreService logVectorStoreService;

    public EmbeddingService(LogVectorStoreService logVectorStoreService) {
        this.logVectorStoreService = logVectorStoreService;
    }

    @Async("embeddingExecutor")
    @TransactionalEventListener
    public void indexLogsAsync(LogService.LogsIngested event) {
        try {
            logVectorStoreService.indexLogs(event.entries());
        } catch (Exception ex) {
            log.error("Log ingestion succeeded but embedding indexing failed", ex);
        }
    }
}
