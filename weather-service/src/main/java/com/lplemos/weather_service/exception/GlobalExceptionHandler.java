package com.lplemos.weather_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the WeatherService API
 * Handles all exceptions and returns appropriate HTTP responses
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle WeatherServiceException and its subclasses
     */
    @ExceptionHandler(WeatherServiceException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleWeatherServiceException(WeatherServiceException ex) {
        logger.error("WeatherServiceException: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getHttpStatus()
        );
        
        return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(errorResponse));
    }
    
    /**
     * Handle CityNotFoundException specifically
     */
    @ExceptionHandler(CityNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleCityNotFoundException(CityNotFoundException ex) {
        logger.warn("CityNotFoundException: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getHttpStatus()
        );
        
        return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(errorResponse));
    }
    
    /**
     * Handle WeatherProviderException specifically
     */
    @ExceptionHandler(WeatherProviderException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleWeatherProviderException(WeatherProviderException ex) {
        logger.error("WeatherProviderException: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getHttpStatus()
        );
        
        return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(errorResponse));
    }
    
    /**
     * Handle InvalidRequestException specifically
     */
    @ExceptionHandler(InvalidRequestException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleInvalidRequestException(InvalidRequestException ex) {
        logger.warn("InvalidRequestException: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getHttpStatus()
        );
        
        return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(errorResponse));
    }
    
    /**
     * Handle CacheException specifically
     */
    @ExceptionHandler(CacheException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleCacheException(CacheException ex) {
        logger.error("CacheException: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getHttpStatus()
        );
        
        return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(errorResponse));
    }
    
    /**
     * Handle WebExchangeBindException (validation errors)
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(WebExchangeBindException ex) {
        logger.warn("Validation error: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed",
            HttpStatus.BAD_REQUEST.value()
        );
        
        // Add field-specific validation errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getFieldErrors().forEach(fieldError -> 
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        errorResponse.put("fieldErrors", fieldErrors);
        
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    
    /**
     * Handle HandlerMethodValidationException (method parameter validation errors)
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        logger.warn("Handler method validation error: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed",
            HttpStatus.BAD_REQUEST.value()
        );
        
        // Add field-specific validation errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getAllValidationResults().forEach(result -> {
            String fieldName = result.getMethodParameter().getParameterName();
            String errorMessage = result.getResolvableErrors().isEmpty() ? 
                "Invalid value" : result.getResolvableErrors().get(0).getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        errorResponse.put("fieldErrors", fieldErrors);
        
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    
    /**
     * Handle ServerWebInputException (malformed requests)
     */
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleServerWebInputException(ServerWebInputException ex) {
        logger.warn("ServerWebInputException: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            "MALFORMED_REQUEST",
            "Request is malformed or contains invalid data",
            HttpStatus.BAD_REQUEST.value()
        );
        
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    
    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("IllegalArgumentException: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            "INVALID_ARGUMENT",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    
    /**
     * Handle all other exceptions (fallback)
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
    
    /**
     * Create a standardized error response
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message, int status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status);
        errorResponse.put("error", HttpStatus.valueOf(status).getReasonPhrase());
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("path", "/api/v1/weather"); // This will be overridden by actual path
        return errorResponse;
    }
} 