package com.lplemos.weather_service.service;

import reactor.core.publisher.Mono;
import java.util.Map;

public interface HierarchicalCacheService {
    
    /**
     * Gets current weather with hierarchical caching
     */
    Mono<Map<String, Object>> getCurrentWeather(String cityName, String providerType, String language);
    
    /**
     * Gets current weather by coordinates with hierarchical caching
     */
    Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, String providerType, String language);
    
    /**
     * Gets weather forecast with hierarchical caching
     */
    Mono<Map<String, Object>> getWeatherForecast(String cityName, String providerType, String language);
    
    /**
     * Gets weather forecast by coordinates with hierarchical caching
     */
    Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, String providerType, String language);
    
    /**
     * Evicts data from both local and Redis caches for a city
     */
    Mono<Boolean> evictCityCache(String cityName);
    
    /**
     * Evicts all data from both caches
     */
    Mono<Boolean> evictAllCache();
    
    /**
     * Gets statistics from both cache layers
     */
    Mono<Map<String, Object>> getCacheStats();
    
    /**
     * Checks availability of both cache layers
     */
    Mono<Map<String, Boolean>> getCacheHealth();
} 