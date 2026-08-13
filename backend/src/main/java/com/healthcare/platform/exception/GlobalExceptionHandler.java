package com.healthcare.platform.exception;

import com.healthcare.platform.config.CorrelationIdFilter;
import com.healthcare.platform.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Centralized exception handling (Phase 18): consistent error shape, proper
 * HTTP status codes, and — critically — no leaking of stack traces or raw
 * database errors to API clients. Every response includes the request's
 * correlation ID so a client-reported error can be traced back to the exact
 * server-side log lines.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidFeedNameException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFeed(InvalidFeedNameException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(AsyncRequestTimeoutException ex, HttpServletRequest request) {
        return build(HttpStatus.GATEWAY_TIMEOUT, "Request timed out", request, null);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        log.error("Database error [correlationId={}]", org.slf4j.MDC.get(CorrelationIdFilter.MDC_KEY), ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "A database error occurred. Please try again.", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception [correlationId={}]", org.slf4j.MDC.get(CorrelationIdFilter.MDC_KEY), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request, List<String> details) {
        String correlationId = org.slf4j.MDC.get(CorrelationIdFilter.MDC_KEY);
        ErrorResponse body = new ErrorResponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI(), correlationId, details);
        return ResponseEntity.status(status).body(body);
    }
}