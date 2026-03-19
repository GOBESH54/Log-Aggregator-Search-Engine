package com.logaggregator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class LogIngestRequest {

    @NotBlank(message = "Source cannot be blank")
    @Size(max = 255, message = "Source must be less than 255 characters")
    private String source;

    @NotBlank(message = "Level cannot be blank")
    @Pattern(regexp = "(?i)^(ERROR|WARN|INFO|DEBUG|TRACE)$", message = "Level must be ERROR, WARN, INFO, DEBUG, or TRACE")
    private String level;

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 10000, message = "Message must be less than 10000 characters")
    private String message;

    private LocalDateTime timestamp;

    @Size(max = 10000, message = "Metadata must be less than 10000 characters")
    private String metadata;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
