package com.logaggregator.service;

import com.logaggregator.config.LogProperties;
import com.logaggregator.model.AiLogAnalysisRequest;
import com.logaggregator.model.AiLogAnalysisResult;
import com.logaggregator.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LogAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LogAnalysisService.class);
    private static final String DEFAULT_ANALYSIS_PROMPT = "Analyze these logs and summarize any issues.";

    private final LogService logService;
    private final ChatClient chatClient;
    private final LogProperties logProperties;

    public LogAnalysisService(LogService logService, ChatClient.Builder chatClientBuilder, LogProperties logProperties) {
        this.logService = logService;
        this.chatClient = chatClientBuilder.build();
        this.logProperties = logProperties;
    }

    public AiLogAnalysisResult analyzeLogsStructured(AiLogAnalysisRequest request) {
        logProperties.getAi().requireEnabled();
        log.info("Fetching logs for structured AI analysis...");
        List<LogEntry> logs = fetchLogsForAnalysis(request);

        if (logs.isEmpty()) {
            return new AiLogAnalysisResult(
                "No logs found for the given criteria.",
                AiLogAnalysisResult.Severity.LOW,
                "No immediate action needed."
            );
        }

        String logsText = formatLogs(logs);
        String systemPrompt = buildSystemPrompt(logsText);
        BeanOutputConverter<AiLogAnalysisResult> converter = new BeanOutputConverter<>(AiLogAnalysisResult.class);
        String userPrompt = buildStructuredPrompt(request, converter);

        log.info("Sending structured prompt to local Ollama model...");
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(converter);
    }

    public Flux<String> streamLogAnalysis(AiLogAnalysisRequest request) {
        logProperties.getAi().requireEnabled();
        return Mono.fromCallable(() -> fetchLogsForAnalysis(request))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(logs -> {
                    if (logs.isEmpty()) {
                        return Flux.just("No logs found for the given criteria to analyze.");
                    }

                    String logsText = formatLogs(logs);
                    String systemPrompt = buildSystemPrompt(logsText);
                    String userPrompt = resolvePrompt(request);

                    log.info("Starting streaming analysis for {} logs", logs.size());
                    return chatClient.prompt()
                            .system(systemPrompt)
                            .user(userPrompt)
                            .stream()
                            .content()
                            .filter(Objects::nonNull);
                });
    }

    private String formatLogs(List<LogEntry> logs) {
        return logs.stream()
                .map(logEntry -> String.format(
                        "[%s] %s | %s - %s",
                        logEntry.getTimestamp(),
                        logEntry.getLevel(),
                        logEntry.getSource(),
                        logEntry.getMessage()))
                .collect(Collectors.joining("\n"));
    }

    private String buildSystemPrompt(String logsText) {
        return "You are an expert DevOps AI assistant. "
                + "Analyze the following logs and answer the user's prompt. "
                + "Be concise, technical, and actionable.\n\nLOGS:\n"
                + logsText;
    }

    private String resolvePrompt(AiLogAnalysisRequest request) {
        if (request == null || request.getPrompt() == null || request.getPrompt().isBlank()) {
            return DEFAULT_ANALYSIS_PROMPT;
        }
        return request.getPrompt();
    }

    private String buildStructuredPrompt(AiLogAnalysisRequest request,
                                         BeanOutputConverter<AiLogAnalysisResult> converter) {
        return resolvePrompt(request) + "\n\n"
                + "Return your result in strict JSON only. "
                + "Do not add markdown or explanatory text.\n"
                + converter.getFormat();
    }

    private List<LogEntry> fetchLogsForAnalysis(AiLogAnalysisRequest request) {
        String semanticQuery = resolveSemanticQuery(request);
        if (semanticQuery != null && !semanticQuery.isBlank()) {
            List<LogEntry> semanticResults = logService.semanticSearchLogs(semanticQuery, request);
            if (!semanticResults.isEmpty()) {
                log.info("Using {} semantically retrieved logs for analysis", semanticResults.size());
                return semanticResults;
            }
            log.info("No semantic results found, falling back to keyword/time filter search");
        }
        return logService.searchLogs(request);
    }

    private String resolveSemanticQuery(AiLogAnalysisRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getSemanticQuery() != null && !request.getSemanticQuery().isBlank()) {
            return request.getSemanticQuery();
        }
        if (request.getSearchText() != null && !request.getSearchText().isBlank()) {
            return request.getSearchText();
        }
        return request.getPrompt();
    }
}
