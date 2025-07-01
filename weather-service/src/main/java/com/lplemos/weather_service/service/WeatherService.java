package com.lplemos.weather_service.service;

import com.lplemos.weather_service.integrations.weather.WeatherProvider;
import com.lplemos.weather_service.model.WeatherProviderType;
import com.lplemos.weather_service.model.WeatherResponse;
import com.lplemos.weather_service.model.WeatherSummary;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Service interface for weather data operations
 * Provides a facade for accessing weather data from multiple providers
 */
public interface WeatherService {
    
    /**
     * Get current weather using the default provider (OpenWeatherMap)
     */
    Mono<Map<String, Object>> getCurrentWeather(String cityName);
    
    /**
     * Get current weather using a specific provider
     */
    Mono<Map<String, Object>> getCurrentWeather(String cityName, WeatherProviderType providerType);
    
    /**
     * Get current weather using a specific provider and language
     */
    Mono<Map<String, Object>> getCurrentWeather(String cityName, WeatherProviderType providerType, String language);
    
    /**
     * Get current weather (structured) using the default provider
     */
    Mono<WeatherResponse> getCurrentWeatherStructured(String cityName);
    
    /**
     * Get current weather (structured) using a specific provider
     */
    Mono<WeatherResponse> getCurrentWeatherStructured(String cityName, WeatherProviderType providerType);
    
    /**
     * Get weather summary using the default provider
     */
    Mono<WeatherSummary> getWeatherSummary(String cityName);
    
    /**
     * Get weather summary using a specific provider
     */
    Mono<WeatherSummary> getWeatherSummary(String cityName, WeatherProviderType providerType);
    
    /**
     * Get weather forecast using the default provider
     */
    Mono<Map<String, Object>> getWeatherForecast(String cityName);
    
    /**
     * Get weather forecast using a specific provider
     */
    Mono<Map<String, Object>> getWeatherForecast(String cityName, WeatherProviderType providerType);
    
    /**
     * Get weather forecast using a specific provider and language
     */
    Mono<Map<String, Object>> getWeatherForecast(String cityName, WeatherProviderType providerType, String language);
    
    /**
     * Get current weather by city ID using the default provider
     */
    Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId);
    
    /**
     * Get current weather by city ID using a specific provider
     */
    Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId, WeatherProviderType providerType);
    
    /**
     * Get current weather by coordinates using the default provider
     */
    Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon);
    
    /**
     * Get current weather by coordinates using a specific provider
     */
    Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, WeatherProviderType providerType);
    
    /**
     * Get current weather by coordinates using a specific provider and language
     */
    Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, WeatherProviderType providerType, String language);
    
    /**
     * Get weather forecast by coordinates using the default provider
     */
    Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon);
    
    /**
     * Get weather forecast by coordinates using a specific provider
     */
    Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, WeatherProviderType providerType);
    
    /**
     * Get weather forecast by coordinates using a specific provider and language
     */
    Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, WeatherProviderType providerType, String language);
    
    /**
     * Get the default weather provider
     */
    WeatherProvider getDefaultProvider();
    
    /**
     * Get a specific weather provider
     */
    WeatherProvider getProvider(WeatherProviderType providerType);
    
    /**
     * Get list of available providers
     */
    List<String> getAvailableProviders();
    
    /**
     * Check if a specific provider is available
     */
    Mono<Boolean> isProviderAvailable(WeatherProviderType providerType);
} 