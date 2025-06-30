package com.lplemos.weather_service.service;

import reactor.core.publisher.Mono;
import java.util.Map;

public interface CacheService {
    
    /**
     * Evicts cache entries for a specific city
     * @param cityName the city name
     * @return Mono<Boolean> indicating success
     */
    Mono<Boolean> evictCityCache(String cityName);
    
    /**
     * Evicts all weather cache entries
     * @return Mono<Boolean> indicating success
     */
    Mono<Boolean> evictAllWeatherCache();
    
    /**
     * Gets cache statistics
     * @return Mono<Map<String, Object>> with cache stats
     */
    Mono<Map<String, Object>> getCacheStats();
    
    /**
     * Checks if cache is available
     * @return Mono<Boolean> indicating cache availability
     */
    Mono<Boolean> isCacheAvailable();
} 