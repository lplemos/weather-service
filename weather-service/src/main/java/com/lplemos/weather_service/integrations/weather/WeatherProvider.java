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
     * Get current weather data for a city with language
     * @param cityName the name of the city
     * @param language the language code
     * @return current weather data
     */
    Mono<Map<String, Object>> getCurrentWeather(String cityName, String language);
    
    /**
     * Get current weather data for a city (structured)
     * @param cityName the name of the city
     * @return structured weather data
     */
    Mono<WeatherResponse> getCurrentWeatherStructured(String cityName);
    
    /**
     * Get current weather data for a city (structured) with language
     * @param cityName the name of the city
     * @param language the language code
     * @return structured weather data
     */
    Mono<WeatherResponse> getCurrentWeatherStructured(String cityName, String language);
    
    /**
     * Get weather summary for a city
     * @param cityName the name of the city
     * @return weather summary
     */
    Mono<WeatherSummary> getWeatherSummary(String cityName);
    
    /**
     * Get weather summary for a city with language
     * @param cityName the name of the city
     * @param language the language code
     * @return weather summary
     */
    Mono<WeatherSummary> getWeatherSummary(String cityName, String language);
    
    /**
     * Get 5-day weather forecast for a city
     * @param cityName the name of the city
     * @return forecast data
     */
    Mono<Map<String, Object>> getWeatherForecast(String cityName);
    
    /**
     * Get 5-day weather forecast for a city with language
     * @param cityName the name of the city
     * @param language the language code
     * @return forecast data
     */
    Mono<Map<String, Object>> getWeatherForecast(String cityName, String language);
    
    /**
     * Get current weather by city ID
     * @param cityId the city ID
     * @return current weather data
     */
    Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId);
    
    /**
     * Get current weather by city ID with language
     * @param cityId the city ID
     * @param language the language code
     * @return current weather data
     */
    Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId, String language);
    
    /**
     * Get current weather by coordinates
     * @param lat the latitude
     * @param lon the longitude
     * @return current weather data
     */
    Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon);
    
    /**
     * Get current weather by coordinates with language
     * @param lat the latitude
     * @param lon the longitude
     * @param language the language code
     * @return current weather data
     */
    Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, String language);
    
    /**
     * Get weather forecast by coordinates
     * @param lat the latitude
     * @param lon the longitude
     * @return forecast data
     */
    Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon);
    
    /**
     * Get weather forecast by coordinates with language
     * @param lat the latitude
     * @param lon the longitude
     * @param language the language code
     * @return forecast data
     */
    Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, String language);
    
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