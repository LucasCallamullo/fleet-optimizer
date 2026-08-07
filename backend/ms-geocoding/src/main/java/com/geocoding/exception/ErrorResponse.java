package com.geocoding.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String path
) {
    public ErrorResponse(int status, String error, String path) {
        this(LocalDateTime.now(), status, error, path);
    }
}