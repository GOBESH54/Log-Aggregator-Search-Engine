package com.logaggregator.controller;

import com.logaggregator.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, WebExchangeBindException.class})
    public ResponseEntity<ApiErrorResponse> handleValidationException(Exception ex, ServerWebExchange exchange) {
        List<String> details;

        if (ex instanceof MethodArgumentNotValidException servletException) {
            details = servletException.getBindingResult().getFieldErrors().stream()
                    .map(this::mapFieldError)
                    .toList();
        } else {
            WebExchangeBindException webfluxException = (WebExchangeBindException) ex;
            details = webfluxException.getFieldErrors().stream()
                    .map(this::mapFieldError)
                    .toList();
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", exchange, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      ServerWebExchange exchange) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", exchange, details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ServerWebInputException.class})
    public ResponseEntity<ApiErrorResponse> handleMalformedPayload(Exception ex, ServerWebExchange exchange) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request payload", exchange, List.of(ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex,
                                                                 ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String reason = ex.getReason() == null || ex.getReason().isBlank()
                ? "Request failed"
                : ex.getReason();
        return buildResponse(status, reason, exchange, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                                  ServerWebExchange exchange) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex, ServerWebExchange exchange) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected server error",
                exchange,
                List.of(ex.getClass().getSimpleName()));
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status,
                                                           String message,
                                                           ServerWebExchange exchange,
                                                           List<String> details) {
        ApiErrorResponse response = new ApiErrorResponse();
        response.setStatus(status.value());
        response.setError(status.getReasonPhrase());
        response.setMessage(message);
        response.setPath(exchange.getRequest().getPath().value());
        response.setDetails(details);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    private String mapFieldError(FieldError fieldError) {
        String errorMessage = fieldError.getDefaultMessage() == null
                ? "Invalid value"
                : fieldError.getDefaultMessage();
        return fieldError.getField() + ": " + errorMessage;
    }
}
