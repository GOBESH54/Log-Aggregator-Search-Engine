package com.logaggregator.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_insights")
public class AiInsight {

    public enum Severity {
        HIGH,
        MEDIUM,
        LOW
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "insight_type", nullable = false)
    private String insightType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "analysis_window_start", nullable = false)
    private LocalDateTime analysisWindowStart;

    @Column(name = "analysis_window_end", nullable = false)
    private LocalDateTime analysisWindowEnd;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInsightType() {
        return insightType;
    }

    public void setInsightType(String insightType) {
        this.insightType = insightType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public LocalDateTime getAnalysisWindowStart() {
        return analysisWindowStart;
    }

    public void setAnalysisWindowStart(LocalDateTime analysisWindowStart) {
        this.analysisWindowStart = analysisWindowStart;
    }

    public LocalDateTime getAnalysisWindowEnd() {
        return analysisWindowEnd;
    }

    public void setAnalysisWindowEnd(LocalDateTime analysisWindowEnd) {
        this.analysisWindowEnd = analysisWindowEnd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
