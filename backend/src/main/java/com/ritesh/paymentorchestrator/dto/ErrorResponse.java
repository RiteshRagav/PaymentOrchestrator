package com.ritesh.paymentorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Standard error response structure returned by all exception handlers.
 */
@Schema(description = "Standard API error response")
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Human-readable error message", example = "Amount must be greater than zero")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/payments")
    private String path;

    public ErrorResponse() {}

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public static ErrorResponse of(int status, String error, String message, String path) {
        ErrorResponse r = new ErrorResponse();
        r.timestamp = LocalDateTime.now();
        r.status = status;
        r.error = error;
        r.message = message;
        r.path = path;
        return r;
    }
}
