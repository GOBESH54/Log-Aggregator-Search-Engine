package com.logaggregator.model;

public class AiLogAnalysisResult {

    public enum Severity {
        HIGH,
        MEDIUM,
        LOW
    }

    private String rootCause;
    private Severity severity;
    private String recommendedAction;

    public AiLogAnalysisResult() {
    }

    public AiLogAnalysisResult(String rootCause, Severity severity, String recommendedAction) {
        this.rootCause = rootCause;
        this.severity = severity;
        this.recommendedAction = recommendedAction;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }
}
