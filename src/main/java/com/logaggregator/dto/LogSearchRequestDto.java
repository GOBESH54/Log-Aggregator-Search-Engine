package com.logaggregator.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class LogSearchRequestDto {

    @Size(max = 300, message = "searchText must be less than 300 characters")
    private String searchText;

    @Size(max = 300, message = "semanticQuery must be less than 300 characters")
    private String semanticQuery;

    private Boolean semanticSearch = false;

    @Min(value = 1, message = "semanticTopK must be at least 1")
    @Max(value = 1000, message = "semanticTopK must be less than or equal to 1000")
    private Integer semanticTopK = 50;

    @Size(max = 255, message = "source must be less than 255 characters")
    private String source;

    @Pattern(regexp = "(?i)^(ERROR|WARN|INFO|DEBUG|TRACE)$", message = "level must be ERROR, WARN, INFO, DEBUG, or TRACE")
    private String level;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Min(value = 0, message = "page must be at least 0")
    private Integer page = 0;

    @Min(value = 1, message = "size must be at least 1")
    @Max(value = 1000, message = "size must be less than or equal to 1000")
    private Integer size = 100;

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getSemanticQuery() {
        return semanticQuery;
    }

    public void setSemanticQuery(String semanticQuery) {
        this.semanticQuery = semanticQuery;
    }

    public Boolean getSemanticSearch() {
        return semanticSearch;
    }

    public void setSemanticSearch(Boolean semanticSearch) {
        this.semanticSearch = semanticSearch;
    }

    public Integer getSemanticTopK() {
        return semanticTopK;
    }

    public void setSemanticTopK(Integer semanticTopK) {
        this.semanticTopK = semanticTopK;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
