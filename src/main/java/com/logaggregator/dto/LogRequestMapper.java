package com.logaggregator.dto;

import com.logaggregator.model.AiLogAnalysisRequest;
import com.logaggregator.model.LogEntry;
import com.logaggregator.model.LogSearchRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class LogRequestMapper {

    public LogEntry toEntity(LogIngestRequest request) {
        LogEntry entity = new LogEntry();
        entity.setSource(trimToNull(request.getSource()));
        entity.setLevel(normalizeLevel(request.getLevel()));
        entity.setMessage(trimToNull(request.getMessage()));
        entity.setTimestamp(request.getTimestamp());
        entity.setMetadata(trimToNull(request.getMetadata()));
        return entity;
    }

    public LogEntryResponse toResponse(LogEntry entry) {
        LogEntryResponse response = new LogEntryResponse();
        response.setId(entry.getId());
        response.setSource(entry.getSource());
        response.setLevel(entry.getLevel());
        response.setMessage(entry.getMessage());
        response.setTimestamp(entry.getTimestamp());
        response.setMetadata(entry.getMetadata());
        return response;
    }

    public List<LogEntryResponse> toResponses(List<LogEntry> entries) {
        return entries.stream().map(this::toResponse).toList();
    }

    public LogSearchRequest toDomain(LogSearchRequestDto dto) {
        LogSearchRequest request = new LogSearchRequest();
        request.setSearchText(trimToNull(dto.getSearchText()));
        request.setSemanticQuery(trimToNull(dto.getSemanticQuery()));
        request.setSemanticSearch(dto.getSemanticSearch());
        request.setSemanticTopK(dto.getSemanticTopK());
        request.setSource(trimToNull(dto.getSource()));
        request.setLevel(normalizeLevel(dto.getLevel()));
        request.setStartTime(dto.getStartTime());
        request.setEndTime(dto.getEndTime());
        request.setPage(dto.getPage());
        request.setSize(dto.getSize());
        return request;
    }

    public AiLogAnalysisRequest toDomain(AiLogAnalysisRequestDto dto) {
        AiLogAnalysisRequest request = new AiLogAnalysisRequest();
        request.setSearchText(trimToNull(dto.getSearchText()));
        request.setSemanticQuery(trimToNull(dto.getSemanticQuery()));
        request.setSemanticSearch(dto.getSemanticSearch());
        request.setSemanticTopK(dto.getSemanticTopK());
        request.setSource(trimToNull(dto.getSource()));
        request.setLevel(normalizeLevel(dto.getLevel()));
        request.setStartTime(dto.getStartTime());
        request.setEndTime(dto.getEndTime());
        request.setPage(dto.getPage());
        request.setSize(dto.getSize());
        request.setPrompt(trimToNull(dto.getPrompt()));
        return request;
    }

    private String normalizeLevel(String level) {
        String value = trimToNull(level);
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
