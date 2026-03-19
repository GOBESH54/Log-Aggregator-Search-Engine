package com.logaggregator.model;

public class AiAnomalyResponse {

    private String severity;
    private String summary;
    private String recommendedAction;

    public AiAnomalyResponse() {
    }

    public AiAnomalyResponse(String severity, String summary, String recommendedAction) {
        this.severity = severity;
        this.summary = summary;
        this.recommendedAction = recommendedAction;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
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
}
