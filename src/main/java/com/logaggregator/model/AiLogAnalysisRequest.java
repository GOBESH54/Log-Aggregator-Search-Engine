package com.logaggregator.model;

public class AiLogAnalysisRequest extends LogSearchRequest {

    private String prompt;

    public AiLogAnalysisRequest() {
    }

    public AiLogAnalysisRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
