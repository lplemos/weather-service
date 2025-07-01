package com.lplemos.weather_service.service.impl;

import com.lplemos.weather_service.service.CacheService;
import com.lplemos.weather_service.service.WeatherServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class CacheServiceImpl implements CacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);
    
    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public CacheServiceImpl(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public Mono<Boolean> evictCityCache(String cityName) {
        return Mono.fromCallable(() -> {
            try {
                // Evict from weather-current cache
                var currentCache = cacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
                if (currentCache != null) {
                    currentCache.evictIfPresent(cityName + "-OPENWEATHERMAP");
                    currentCache.evictIfPresent(cityName + "-structured-OPENWEATHERMAP");
                    currentCache.evictIfPresent(cityName + "-summary-OPENWEATHERMAP");
                }
                
                // Evict from weather-forecast cache
                var forecastCache = cacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
                if (forecastCache != null) {
                    forecastCache.evictIfPresent(cityName + "-OPENWEATHERMAP");
                }
                
                logger.info(WeatherServiceConstants.LOG_CACHE_EVICT, "City: " + cityName);
                return true;
            } catch (Exception e) {
                logger.error("Error evicting cache for city: {}", cityName, e);
                return false;
            }
        });
    }
    
    @Override
    public Mono<Boolean> evictAllWeatherCache() {
        return Mono.fromCallable(() -> {
            try {
                // Clear all weather caches
                var currentCache = cacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
                if (currentCache != null) {
                    currentCache.clear();
                }
                
                var forecastCache = cacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
                if (forecastCache != null) {
                    forecastCache.clear();
                }
                
                logger.info(WeatherServiceConstants.LOG_CACHE_EVICT, "All weather cache");
                return true;
            } catch (Exception e) {
                logger.error("Error evicting all weather cache", e);
                return false;
            }
        });
    }
    
    @Override
    public Mono<Map<String, Object>> getCacheStats() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();
            
            try {
                // Get cache names
                stats.put("cacheNames", cacheManager.getCacheNames());
                
                // Get Redis info
                var redisInfo = redisTemplate.getConnectionFactory().getConnection().serverCommands().info();
                stats.put("redisInfo", redisInfo);
                
                stats.put("cacheAvailable", true);
                
            } catch (Exception e) {
                logger.error("Error getting cache stats", e);
                stats.put("cacheAvailable", false);
                stats.put("error", e.getMessage());
            }
            
            return stats;
        });
    }
    
    @Override
    public Mono<Boolean> isCacheAvailable() {
        return Mono.fromCallable(() -> {
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                return true;
            } catch (Exception e) {
                logger.warn("Cache not available: {}", e.getMessage());
                return false;
            }
        });
    }
} 