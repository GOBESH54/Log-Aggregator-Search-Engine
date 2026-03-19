package com.logaggregator.model;

import java.time.LocalDateTime;
import java.util.StringJoiner;

public class LogSearchRequest {
    private String searchText;
    private String semanticQuery;
    private Boolean semanticSearch = false;
    private Integer semanticTopK = 50;
    private String source;
    private String level;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 0;
    private Integer size = 100;

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    public String toCacheKey() {
        return new StringJoiner("|")
                .add(nullSafe(searchText))
                .add(nullSafe(semanticQuery))
                .add(String.valueOf(Boolean.TRUE.equals(semanticSearch)))
                .add(String.valueOf(semanticTopK))
                .add(nullSafe(source))
                .add(nullSafe(level))
                .add(String.valueOf(startTime))
                .add(String.valueOf(endTime))
                .add(String.valueOf(page))
                .add(String.valueOf(size))
                .toString();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
