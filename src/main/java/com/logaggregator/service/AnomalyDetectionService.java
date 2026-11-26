package com.logaggregator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logaggregator.config.LogProperties;
import com.logaggregator.model.AiAnomalyResponse;
import com.logaggregator.model.AiInsight;
import com.logaggregator.model.LogEntry;
import com.logaggregator.repository.AiInsightRepository;
import com.logaggregator.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionService.class);
    private static final List<String> SCAN_LEVELS = List.of("ERROR", "WARN");
    private static final String INSIGHT_TYPE = "HOURLY_ANOMALY_SCAN";

    private final LogRepository logRepository;
    private final AiInsightRepository aiInsightRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final LogProperties logProperties;

    public AnomalyDetectionService(LogRepository logRepository,
                                   AiInsightRepository aiInsightRepository,
                                   ChatClient.Builder chatClientBuilder,
                                   ObjectMapper objectMapper,
                                   LogProperties logProperties) {
        this.logRepository = logRepository;
        this.aiInsightRepository = aiInsightRepository;
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.logProperties = logProperties;
    }

    @Transactional
    public void runHourlyAnomalyScan() {
        logProperties.getAi().requireEnabled();
        LocalDateTime windowEnd = LocalDateTime.now();
        LocalDateTime windowStart = windowEnd.minusHours(1);

        List<LogEntry> candidateLogs = logRepository.findRecentByLevels(SCAN_LEVELS, windowStart, windowEnd);
        if (candidateLogs.isEmpty()) {
            log.info("No WARN/ERROR logs found in the last hour, skipping anomaly scan");
            return;
        }

        int maxChunkChars = logProperties.getAi().getAnomaly().getMaxChunkChars();
        List<String> chunks = chunkLogs(candidateLogs, maxChunkChars);
        log.info("Running anomaly scan on {} logs across {} chunk(s)", candidateLogs.size(), chunks.size());

        for (String chunk : chunks) {
            String llmResponse = requestAnomalyAnalysis(chunk);
            if (isNoAnomaly(llmResponse)) {
                continue;
            }

            AiAnomalyResponse parsedResponse = parseAnomalyResponse(llmResponse);
            saveInsight(parsedResponse, llmResponse, windowStart, windowEnd);
        }
    }

    private String requestAnomalyAnalysis(String logsChunk) {
        String systemPrompt = "You are a senior incident-response and security anomaly detector.";
        String userPrompt = "Analyze the following logs from the last hour. "
                + "Identify any critical anomalies, security threats, or cascading failures. "
                + "If everything is normal, reply exactly with NO_ANOMALY. "
                + "If anomaly exists, return strict JSON only in this format: "
                + "{\"severity\":\"HIGH|MEDIUM|LOW\",\"summary\":\"...\",\"recommendedAction\":\"...\"}.\n\n"
                + "LOGS:\n"
                + logsChunk;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    private List<String> chunkLogs(List<LogEntry> logs, int maxChars) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (LogEntry logEntry : logs) {
            String line = String.format("[%s] %s | %s - %s%n",
                    logEntry.getTimestamp(),
                    logEntry.getLevel(),
                    logEntry.getSource(),
                    logEntry.getMessage());

            if (currentChunk.length() > 0 && currentChunk.length() + line.length() > maxChars) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }

            currentChunk.append(line);
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    private boolean isNoAnomaly(String llmResponse) {
        if (llmResponse == null) {
            return true;
        }
        return "NO_ANOMALY".equalsIgnoreCase(llmResponse.trim());
    }

    private AiAnomalyResponse parseAnomalyResponse(String rawResponse) {
        String normalizedResponse = removeMarkdownCodeFence(rawResponse);

        try {
            AiAnomalyResponse parsed = objectMapper.readValue(normalizedResponse, AiAnomalyResponse.class);
            if (parsed.getSummary() == null || parsed.getSummary().isBlank()) {
                parsed.setSummary("Potential anomaly detected by LLM.");
            }
            return parsed;
        } catch (Exception e) {
            log.warn("Unable to parse anomaly JSON response, using fallback summary", e);
            String fallbackSummary = normalizedResponse != null && !normalizedResponse.isBlank()
                    ? normalizedResponse
                    : "Potential anomaly detected by LLM.";
            return new AiAnomalyResponse("MEDIUM", truncate(fallbackSummary, 2000), "Investigate system logs manually.");
        }
    }

    private void saveInsight(AiAnomalyResponse anomalyResponse,
                             String rawResponse,
                             LocalDateTime windowStart,
                             LocalDateTime windowEnd) {
        AiInsight insight = new AiInsight();
        insight.setInsightType(INSIGHT_TYPE);
        insight.setSeverity(parseSeverity(anomalyResponse.getSeverity()));
        insight.setSummary(anomalyResponse.getSummary());
        insight.setRecommendedAction(anomalyResponse.getRecommendedAction());
        insight.setRawResponse(rawResponse);
        insight.setAnalysisWindowStart(windowStart);
        insight.setAnalysisWindowEnd(windowEnd);

        aiInsightRepository.save(insight);
        log.warn("Anomaly insight saved with severity={} for window {} -> {}",
                insight.getSeverity(), windowStart, windowEnd);
    }

    private AiInsight.Severity parseSeverity(String severityRaw) {
        if (severityRaw == null || severityRaw.isBlank()) {
            return AiInsight.Severity.MEDIUM;
        }
        String normalized = severityRaw.trim().toUpperCase(Locale.ROOT);
        try {
            return AiInsight.Severity.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return AiInsight.Severity.MEDIUM;
        }
    }

    private String removeMarkdownCodeFence(String rawResponse) {
        if (rawResponse == null) {
            return "";
        }

        String trimmed = rawResponse.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```[a-zA-Z]*\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
