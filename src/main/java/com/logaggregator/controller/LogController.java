package com.logaggregator.controller;

import com.logaggregator.dto.AiLogAnalysisRequestDto;
import com.logaggregator.dto.LogEntryResponse;
import com.logaggregator.dto.LogIngestRequest;
import com.logaggregator.dto.LogRequestMapper;
import com.logaggregator.dto.LogSearchRequestDto;
import com.logaggregator.model.AiLogAnalysisRequest;
import com.logaggregator.model.AiLogAnalysisResult;
import com.logaggregator.model.LogEntry;
import com.logaggregator.model.LogSearchRequest;
import com.logaggregator.service.AnomalyDetectionService;
import com.logaggregator.service.LogAnalysisService;
import com.logaggregator.service.LogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;
    private final LogAnalysisService logAnalysisService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final LogRequestMapper logRequestMapper;

    public LogController(LogService logService,
                         LogAnalysisService logAnalysisService,
                         AnomalyDetectionService anomalyDetectionService,
                         LogRequestMapper logRequestMapper) {
        this.logService = logService;
        this.logAnalysisService = logAnalysisService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.logRequestMapper = logRequestMapper;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestLogs(@Valid @RequestBody @Size(min = 1, message = "At least one log entry is required")
                                             List<@Valid LogIngestRequest> logs) {
        List<LogEntry> entities = logs.stream()
                .map(logRequestMapper::toEntity)
                .toList();

        logService.ingestLogs(entities);
        return ResponseEntity.ok("Ingested " + entities.size() + " logs");
    }

    @PostMapping("/search")
    public ResponseEntity<List<LogEntryResponse>> searchLogs(@Valid @RequestBody LogSearchRequestDto requestDto) {
        LogSearchRequest request = logRequestMapper.toDomain(requestDto);
        List<LogEntry> results = logService.searchLogs(request);
        return ResponseEntity.ok(logRequestMapper.toResponses(results));
    }

    @PostMapping("/search/semantic")
    public ResponseEntity<List<LogEntryResponse>> semanticSearchLogs(@Valid @RequestBody LogSearchRequestDto requestDto) {
        LogSearchRequest request = logRequestMapper.toDomain(requestDto);
        request.setSemanticSearch(true);
        List<LogEntry> results = logService.searchLogs(request);
        return ResponseEntity.ok(logRequestMapper.toResponses(results));
    }

    @PostMapping("/analyze")
    public ResponseEntity<AiLogAnalysisResult> analyzeLogsWithAi(@Valid @RequestBody AiLogAnalysisRequestDto requestDto) {
        AiLogAnalysisRequest request = logRequestMapper.toDomain(requestDto);
        AiLogAnalysisResult analysis = logAnalysisService.analyzeLogsStructured(request);
        return ResponseEntity.ok(analysis);
    }

    @PostMapping(value = "/analyze/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> analyzeLogsWithAiStream(@Valid @RequestBody AiLogAnalysisRequestDto requestDto) {
        AiLogAnalysisRequest request = logRequestMapper.toDomain(requestDto);
        return logAnalysisService.streamLogAnalysis(request);
    }

    @PostMapping("/anomaly/scan")
    public ResponseEntity<String> runAnomalyScanNow() {
        anomalyDetectionService.runHourlyAnomalyScan();
        return ResponseEntity.ok("Anomaly scan completed for the last hour.");
    }
}
