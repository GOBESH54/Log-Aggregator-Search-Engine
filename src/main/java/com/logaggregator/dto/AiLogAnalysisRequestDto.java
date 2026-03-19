package com.logaggregator.dto;

import jakarta.validation.constraints.Size;

public class AiLogAnalysisRequestDto extends LogSearchRequestDto {

    @Size(max = 2000, message = "prompt must be less than 2000 characters")
    private String prompt;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
