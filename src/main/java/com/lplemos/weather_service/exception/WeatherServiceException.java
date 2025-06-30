package com.lplemos.weather_service.exception;

/**
 * Base exception for WeatherService operations
 */
public class WeatherServiceException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;
    
    public WeatherServiceException(String message) {
        super(message);
        this.errorCode = "WEATHER_SERVICE_ERROR";
        this.httpStatus = 500;
    }
    
    public WeatherServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "WEATHER_SERVICE_ERROR";
        this.httpStatus = 500;
    }
    
    public WeatherServiceException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public WeatherServiceException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
} 