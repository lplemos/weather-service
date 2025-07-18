package com.lplemos.weather_service.cache;

import com.lplemos.weather_service.service.WeatherServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for cache statistics and health monitoring
 * Handles all cache-related metrics and monitoring operations
 */
@Service
public class CacheStatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheStatisticsService.class);
    
    private final CacheManager localCacheManager;
    private final CacheManager redisCacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public CacheStatisticsService(
            CacheManager localCacheManager,
            CacheManager redisCacheManager,
            RedisTemplate<String, Object> redisTemplate) {
        this.localCacheManager = localCacheManager;
        this.redisCacheManager = redisCacheManager;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Gets comprehensive cache statistics
     * @return Mono containing cache statistics
     */
    public Mono<Map<String, Object>> getCacheStats() {
        logger.info("=== CacheStatisticsService.getCacheStats START ===");
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();
            
            try {
                logger.info("Getting local cache stats...");
                addLocalCacheStats(stats);
                
                logger.info("Getting Redis info...");
                addRedisInfo(stats);
                
                stats.put("cacheAvailable", true);
                logger.info("=== CacheStatisticsService.getCacheStats END (SUCCESS) ===");
                
            } catch (Exception e) {
                logger.error("Error getting cache stats: {}", e.getMessage(), e);
                stats.put("cacheAvailable", false);
                stats.put("error", e.getMessage());
                logger.info("=== CacheStatisticsService.getCacheStats END (ERROR) ===");
            }
            
            return stats;
        });
    }
    
    /**
     * Gets cache health status
     * @return Mono containing cache health information
     */
    public Mono<Map<String, Boolean>> getCacheHealth() {
        logger.info("=== CacheStatisticsService.getCacheHealth START ===");
        return Mono.fromCallable(() -> {
            Map<String, Boolean> health = new HashMap<>();
            
            // Check local cache
            logger.info("Checking local cache health...");
            health.put("localCacheAvailable", true); // Local cache is always available
            logger.info("Local cache is available");
            
            // Check Redis cache
            logger.info("Checking Redis cache health...");
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                health.put("redisCacheAvailable", true);
                logger.info("Redis cache is available");
            } catch (Exception e) {
                logger.warn("Redis cache not available: {}", e.getMessage());
                health.put("redisCacheAvailable", false);
            }
            
            logger.info("=== CacheStatisticsService.getCacheHealth END ===");
            return health;
        });
    }
    
    /**
     * Adds local cache statistics to the stats map
     * @param stats the stats map to populate
     */
    private void addLocalCacheStats(Map<String, Object> stats) {
        Cache localCurrentCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
        Cache localForecastCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
        
        if (localCurrentCache != null) {
            Object nativeCache = localCurrentCache.getNativeCache();
            if (nativeCache instanceof java.util.concurrent.ConcurrentHashMap) {
                java.util.concurrent.ConcurrentHashMap<?, ?> map = (java.util.concurrent.ConcurrentHashMap<?, ?>) nativeCache;
                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("size", map.size());
                cacheStats.put("cacheName", localCurrentCache.getName());
                stats.put("localCurrentStats", cacheStats);
                logger.info("Local current cache stats retrieved - Size: {}", map.size());
            }
        } else {
            logger.warn("Local current cache is null");
        }
        
        if (localForecastCache != null) {
            Object nativeCache = localForecastCache.getNativeCache();
            if (nativeCache instanceof java.util.concurrent.ConcurrentHashMap) {
                java.util.concurrent.ConcurrentHashMap<?, ?> map = (java.util.concurrent.ConcurrentHashMap<?, ?>) nativeCache;
                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("size", map.size());
                cacheStats.put("cacheName", localForecastCache.getName());
                stats.put("localForecastStats", cacheStats);
                logger.info("Local forecast cache stats retrieved - Size: {}", map.size());
            }
        } else {
            logger.warn("Local forecast cache is null");
        }
    }
    
    /**
     * Adds Redis information to the stats map
     * @param stats the stats map to populate
     */
    private void addRedisInfo(Map<String, Object> stats) {
        java.util.Properties redisInfo = redisTemplate.getConnectionFactory().getConnection().serverCommands().info();
        stats.put("redisInfo", redisInfo);
        logger.info("Redis info retrieved");
    }
} 