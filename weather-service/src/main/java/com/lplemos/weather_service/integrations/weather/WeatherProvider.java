package com.lplemos.weather_service.integrations.weather;

import com.lplemos.weather_service.model.WeatherResponse;
import com.lplemos.weather_service.model.WeatherSummary;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Common interface for weather data providers
 * This allows us to easily add new weather APIs in the future
 */
public interface WeatherProvider {
    
    /**
     * Get current weather data for a city
     * @param cityName the name of the city
     * @return current weather data
     */
    Mono<Map<String, Object>> getCurrentWeather(String cityName);
    
    /**
     * Get current weather data for a city (structured)
     * @param cityName the name of the city
     * @return structured weather data
     */
    Mono<WeatherResponse> getCurrentWeatherStructured(String cityName);
    
    /**
     * Get weather summary for a city
     * @param cityName the name of the city
     * @return weather summary
     */
    Mono<WeatherSummary> getWeatherSummary(String cityName);
    
    /**
     * Get 5-day weather forecast for a city
     * @param cityName the name of the city
     * @return forecast data
     */
    Mono<Map<String, Object>> getWeatherForecast(String cityName);
    
    /**
     * Get current weather by city ID
     * @param cityId the city ID
     * @return current weather data
     */
    Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId);
    
    /**
     * Get the provider name
     * @return provider name
     */
    String getProviderName();
    
    /**
     * Check if the provider is available/healthy
     * @return true if available
     */
    Mono<Boolean> isAvailable();
} 