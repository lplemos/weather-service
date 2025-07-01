package com.lplemos.weather_service.exception;

/**
 * Exception thrown when request parameters are invalid
 */
public class InvalidRequestException extends WeatherServiceException {
    
    private static final String ERROR_CODE = "INVALID_REQUEST";
    private static final int HTTP_STATUS = 400;
    
    public InvalidRequestException(String message) {
        super("Invalid request: " + message, ERROR_CODE, HTTP_STATUS);
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super("Invalid request: " + message, ERROR_CODE, HTTP_STATUS, cause);
    }
    
    public InvalidRequestException(String field, String reason) {
        super("Invalid request - " + field + ": " + reason, ERROR_CODE, HTTP_STATUS);
    }
} 