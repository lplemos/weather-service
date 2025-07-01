package com.lplemos.weather_service.exception;

/**
 * Exception thrown when a city is not found
 */
public class CityNotFoundException extends WeatherServiceException {
    
    private static final String ERROR_CODE = "CITY_NOT_FOUND";
    private static final int HTTP_STATUS = 404;
    
    public CityNotFoundException(String cityName) {
        super("City not found: " + cityName, ERROR_CODE, HTTP_STATUS);
    }
    
    public CityNotFoundException(String cityName, Throwable cause) {
        super("City not found: " + cityName, ERROR_CODE, HTTP_STATUS, cause);
    }
    
    public CityNotFoundException(Integer cityId) {
        super("City not found with ID: " + cityId, ERROR_CODE, HTTP_STATUS);
    }
    
    public CityNotFoundException(Integer cityId, Throwable cause) {
        super("City not found with ID: " + cityId, ERROR_CODE, HTTP_STATUS, cause);
    }
} 