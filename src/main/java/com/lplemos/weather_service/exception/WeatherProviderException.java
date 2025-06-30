package com.lplemos.weather_service.exception;

/**
 * Exception thrown when there are issues with weather providers
 */
public class WeatherProviderException extends WeatherServiceException {
    
    private static final String ERROR_CODE = "WEATHER_PROVIDER_ERROR";
    private static final int HTTP_STATUS = 503;
    
    public WeatherProviderException(String providerName, String message) {
        super("Weather provider error for " + providerName + ": " + message, ERROR_CODE, HTTP_STATUS);
    }
    
    public WeatherProviderException(String providerName, String message, Throwable cause) {
        super("Weather provider error for " + providerName + ": " + message, ERROR_CODE, HTTP_STATUS, cause);
    }
    
    public WeatherProviderException(String message, Throwable cause) {
        super("Weather provider error: " + message, ERROR_CODE, HTTP_STATUS, cause);
    }
} 