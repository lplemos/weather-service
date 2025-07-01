package com.lplemos.weather_service.exception;

/**
 * Exception thrown when there are cache-related issues
 */
public class CacheException extends WeatherServiceException {
    
    private static final String ERROR_CODE = "CACHE_ERROR";
    private static final int HTTP_STATUS = 503;
    
    public CacheException(String message) {
        super("Cache error: " + message, ERROR_CODE, HTTP_STATUS);
    }
    
    public CacheException(String message, Throwable cause) {
        super("Cache error: " + message, ERROR_CODE, HTTP_STATUS, cause);
    }
    
    public CacheException(String operation, String cacheType, Throwable cause) {
        super("Cache error during " + operation + " on " + cacheType, ERROR_CODE, HTTP_STATUS, cause);
    }
} 