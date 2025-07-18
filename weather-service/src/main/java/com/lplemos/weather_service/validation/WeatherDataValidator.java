package com.lplemos.weather_service.validation;

import com.lplemos.weather_service.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
public class WeatherDataValidator {
    
    private static final String CITY_NAME_PATTERN = "^[a-zA-ZÀ-ÿ\\s\\-',]+$";
    
    /**
     * Validates city name input
     * @param cityName the city name to validate
     * @throws InvalidRequestException if validation fails
     */
    public void validateCityName(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new InvalidRequestException("City name cannot be empty");
        }
        
        if (!cityName.matches(CITY_NAME_PATTERN)) {
            throw new InvalidRequestException("City name can only contain letters, spaces, hyphens, apostrophes, and commas");
        }
    }
    
    /**
     * Validates coordinate inputs
     * @param lat latitude
     * @param lon longitude
     * @throws InvalidRequestException if validation fails
     */
    public void validateCoordinates(Double lat, Double lon) {
        if (lat == null || lon == null) {
            throw new InvalidRequestException("Latitude and longitude cannot be null");
        }
        
        if (lat < -90 || lat > 90) {
            throw new InvalidRequestException("Latitude must be between -90 and 90");
        }
        
        if (lon < -180 || lon > 180) {
            throw new InvalidRequestException("Longitude must be between -180 and 180");
        }
    }
    
    /**
     * Validates that the weather service is available
     * @param weatherService the weather service to validate
     * @throws IllegalStateException if weather service is null
     */
    public void validateWeatherService(Object weatherService) {
        if (weatherService == null) {
            throw new IllegalStateException("WeatherService not injected");
        }
    }
} 