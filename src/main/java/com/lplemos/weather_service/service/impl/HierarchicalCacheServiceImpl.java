package com.lplemos.weather_service.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.lplemos.weather_service.service.HierarchicalCacheService;
import com.lplemos.weather_service.service.WeatherService;
import com.lplemos.weather_service.service.WeatherServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class HierarchicalCacheServiceImpl implements HierarchicalCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(HierarchicalCacheServiceImpl.class);
    
    private final CacheManager localCacheManager;
    private final CacheManager redisCacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WeatherService weatherService;
    
    public HierarchicalCacheServiceImpl(
            CacheManager localCacheManager,
            CacheManager redisCacheManager,
            RedisTemplate<String, Object> redisTemplate,
            WeatherService weatherService) {
        this.localCacheManager = localCacheManager;
        this.redisCacheManager = redisCacheManager;
        this.redisTemplate = redisTemplate;
        this.weatherService = weatherService;
        
        logger.info("HierarchicalCacheServiceImpl initialized with:");
        logger.info("  - LocalCacheManager: {}", localCacheManager != null ? localCacheManager.getClass().getSimpleName() : "NULL");
        logger.info("  - RedisCacheManager: {}", redisCacheManager != null ? redisCacheManager.getClass().getSimpleName() : "NULL");
        logger.info("  - RedisTemplate: {}", redisTemplate != null ? redisTemplate.getClass().getSimpleName() : "NULL");
        logger.info("  - WeatherService: {}", weatherService != null ? weatherService.getClass().getSimpleName() : "NULL");
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName, String providerType) {
        String cacheKey = cityName + "-current-" + providerType;
        logger.info("=== HierarchicalCache.getCurrentWeather START ===");
        logger.info("  City: {} | Provider: {} | CacheKey: {}", cityName, providerType, cacheKey);
        if (weatherService == null) {
            logger.error("HierarchicalCache: WeatherService is null!");
            return Mono.error(new IllegalStateException("WeatherService not injected"));
        }
        return Mono.defer(() -> {
            Map<String, Object> localResult = getFromLocalCache(cacheKey);
            if (localResult != null) {
                logger.info("  Local cache HIT for key: {}", cacheKey);
                return Mono.just(localResult);
            }
            logger.info("  Local cache MISS for key: {}", cacheKey);
            return getFromRedisCache(cacheKey)
                    .flatMap(redisResult -> {
                        if (redisResult != null) {
                            logger.info("  Redis cache HIT for key: {}", cacheKey);
                            putInLocalCache(cacheKey, redisResult);
                            return Mono.just(redisResult);
                        }
                        logger.info("  Redis cache MISS for key: {}", cacheKey);
                        logger.info("  Calling external API for city: {}", cityName);
                        return weatherService.getCurrentWeather(cityName)
                                .flatMap(apiResult -> {
                                    logger.info("  API call SUCCESS for city: {}", cityName);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("  API call FAILED for city: {}", cityName, error);
                                });
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        logger.info("  Redis cache MISS for key: {} (switchIfEmpty)", cacheKey);
                        logger.info("  Calling external API for city: {}", cityName);
                        return weatherService.getCurrentWeather(cityName)
                                .flatMap(apiResult -> {
                                    logger.info("  API call SUCCESS for city: {}", cityName);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("  API call FAILED for city: {}", cityName, error);
                                });
                    }))
                    .doOnError(error -> {
                        logger.error("  Redis cache check FAILED for key: {}", cacheKey, error);
                    });
        });
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName, String providerType) {
        String cacheKey = cityName + "-forecast-" + providerType;
        logger.info("=== HierarchicalCache.getWeatherForecast START ===");
        logger.info("  City: {} | Provider: {} | CacheKey: {}", cityName, providerType, cacheKey);
        return Mono.defer(() -> {
            Map<String, Object> localResult = getFromLocalCache(cacheKey);
            if (localResult != null) {
                logger.info("  Local cache HIT for key: {}", cacheKey);
                return Mono.just(localResult);
            }
            logger.info("  Local cache MISS for key: {}", cacheKey);
            return getFromRedisCache(cacheKey)
                    .flatMap(redisResult -> {
                        if (redisResult != null) {
                            logger.info("  Redis cache HIT for key: {}", cacheKey);
                            putInLocalCache(cacheKey, redisResult);
                            return Mono.just(redisResult);
                        }
                        logger.info("  Redis cache MISS for key: {}", cacheKey);
                        logger.info("  Calling external API for city: {}", cityName);
                        return weatherService.getWeatherForecast(cityName)
                                .flatMap(apiResult -> {
                                    logger.info("  API call SUCCESS for city: {}", cityName);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("  API call FAILED for city: {}", cityName, error);
                                });
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        logger.info("  Redis cache MISS for key: {} (switchIfEmpty)", cacheKey);
                        logger.info("  Calling external API for city: {}", cityName);
                        return weatherService.getWeatherForecast(cityName)
                                .flatMap(apiResult -> {
                                    logger.info("  API call SUCCESS for city: {}", cityName);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("  API call FAILED for city: {}", cityName, error);
                                });
                    }))
                    .doOnError(error -> {
                        logger.error("  Redis cache check FAILED for key: {}", cacheKey, error);
                    });
        });
    }
    
    @Override
    public Mono<Boolean> evictCityCache(String cityName) {
        return Mono.fromCallable(() -> {
            try {
                // Evict from local cache
                var localCurrentCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
                var localForecastCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
                
                if (localCurrentCache != null) {
                    localCurrentCache.evictIfPresent(cityName + "-current-OPENWEATHERMAP");
                }
                if (localForecastCache != null) {
                    localForecastCache.evictIfPresent(cityName + "-forecast-OPENWEATHERMAP");
                }
                
                // Evict from Redis cache
                var redisCurrentCache = redisCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
                var redisForecastCache = redisCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
                
                if (redisCurrentCache != null) {
                    redisCurrentCache.evictIfPresent(cityName + "-current-OPENWEATHERMAP");
                }
                if (redisForecastCache != null) {
                    redisForecastCache.evictIfPresent(cityName + "-forecast-OPENWEATHERMAP");
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
    public Mono<Boolean> evictAllCache() {
        return Mono.fromCallable(() -> {
            try {
                // Clear local caches
                localCacheManager.getCacheNames().forEach(name -> {
                    var cache = localCacheManager.getCache(name);
                    if (cache != null) {
                        cache.clear();
                    }
                });
                
                // Clear Redis caches
                redisCacheManager.getCacheNames().forEach(name -> {
                    var cache = redisCacheManager.getCache(name);
                    if (cache != null) {
                        cache.clear();
                    }
                });
                
                logger.info(WeatherServiceConstants.LOG_CACHE_EVICT, "All cache");
                return true;
            } catch (Exception e) {
                logger.error("Error evicting all cache", e);
                return false;
            }
        });
    }
    
    @Override
    public Mono<Map<String, Object>> getCacheStats() {
        logger.info("=== HierarchicalCache.getCacheStats START ===");
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();
            
            try {
                logger.info("  Getting local cache stats...");
                // Local cache stats (Caffeine)
                var localCurrentCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
                var localForecastCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
                
                if (localCurrentCache instanceof CaffeineCache) {
                    Cache<Object, Object> nativeCache = ((CaffeineCache) localCurrentCache).getNativeCache();
                    stats.put("localCurrentStats", nativeCache.stats());
                    logger.info("  ✅ Local current cache stats retrieved");
                } else {
                    logger.warn("  ❌ Local current cache is not a CaffeineCache: {}", localCurrentCache != null ? localCurrentCache.getClass().getSimpleName() : "NULL");
                }
                
                if (localForecastCache instanceof CaffeineCache) {
                    Cache<Object, Object> nativeCache = ((CaffeineCache) localForecastCache).getNativeCache();
                    stats.put("localForecastStats", nativeCache.stats());
                    logger.info("  ✅ Local forecast cache stats retrieved");
                } else {
                    logger.warn("  ❌ Local forecast cache is not a CaffeineCache: {}", localForecastCache != null ? localForecastCache.getClass().getSimpleName() : "NULL");
                }
                
                logger.info("  Getting Redis info...");
                // Redis info
                var redisInfo = redisTemplate.getConnectionFactory().getConnection().serverCommands().info();
                stats.put("redisInfo", redisInfo);
                logger.info("  ✅ Redis info retrieved");
                
                stats.put("cacheAvailable", true);
                logger.info("=== HierarchicalCache.getCacheStats END (SUCCESS) ===");
                
            } catch (Exception e) {
                logger.error("  ❌ Error getting cache stats: {}", e.getMessage(), e);
                stats.put("cacheAvailable", false);
                stats.put("error", e.getMessage());
                logger.info("=== HierarchicalCache.getCacheStats END (ERROR) ===");
            }
            
            return stats;
        });
    }
    
    @Override
    public Mono<Map<String, Boolean>> getCacheHealth() {
        logger.info("=== HierarchicalCache.getCacheHealth START ===");
        return Mono.fromCallable(() -> {
            Map<String, Boolean> health = new HashMap<>();
            
            // Check local cache
            logger.info("  Checking local cache health...");
            health.put("localCacheAvailable", true); // Local cache is always available
            logger.info("  ✅ Local cache is available");
            
            // Check Redis cache
            logger.info("  Checking Redis cache health...");
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                health.put("redisCacheAvailable", true);
                logger.info("  ✅ Redis cache is available");
            } catch (Exception e) {
                logger.warn("  ❌ Redis cache not available: {}", e.getMessage());
                health.put("redisCacheAvailable", false);
            }
            
            logger.info("=== HierarchicalCache.getCacheHealth END ===");
            return health;
        });
    }
    
    // Helper methods
    @SuppressWarnings("unchecked")
    private Map<String, Object> getFromLocalCache(String key) {
        logger.debug("  getFromLocalCache: Checking key: {}", key);
        try {
            // Determine which cache to use based on the key
            String cacheName = key.contains("-forecast-") ? 
                WeatherServiceConstants.CACHE_WEATHER_FORECAST : 
                WeatherServiceConstants.CACHE_WEATHER_CURRENT;
            
            logger.debug("  getFromLocalCache: Using cache name: {}", cacheName);
            
            var cache = localCacheManager.getCache(cacheName);
            if (cache != null) {
                logger.debug("  getFromLocalCache: Cache found, getting value...");
                var value = cache.get(key);
                if (value != null && value.get() instanceof Map) {
                    Map<String, Object> result = (Map<String, Object>) value.get();
                    logger.debug("  getFromLocalCache: ✅ Found value in local cache, size: {}", result.size());
                    return result;
                } else {
                    logger.debug("  getFromLocalCache: ❌ No value found or value is not a Map");
                }
            } else {
                logger.warn("  getFromLocalCache: ❌ Cache '{}' not found in localCacheManager", cacheName);
            }
        } catch (Exception e) {
            logger.error("  getFromLocalCache: ❌ Error getting from local cache: {}", e.getMessage(), e);
        }
        logger.debug("  getFromLocalCache: Returning null");
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Mono<Map<String, Object>> getFromRedisCache(String key) {
        logger.debug("  getFromRedisCache: Checking key: {}", key);
        
        // Simplificar drasticamente - sempre retornar empty por agora
        return Mono.<Map<String, Object>>empty()
                .doOnNext(result -> {
                    logger.debug("  getFromRedisCache: doOnNext called with: {}", result);
                })
                .doOnSuccess(result -> {
                    logger.debug("  getFromRedisCache: doOnSuccess called with: {}", result);
                });
        
        /*
        // CÓDIGO ORIGINAL - COMENTADO PARA REFERÊNCIA
        return Mono.fromCallable(() -> {
            logger.debug("  getFromRedisCache: Starting fromCallable for key: {}", key);
            try {
                // Determine which cache to use based on the key
                String cacheName = key.contains("-forecast-") ? 
                    WeatherServiceConstants.CACHE_WEATHER_FORECAST : 
                    WeatherServiceConstants.CACHE_WEATHER_CURRENT;
                
                logger.debug("  getFromRedisCache: Using cache name: {}", cacheName);
                
                var cache = redisCacheManager.getCache(cacheName);
                logger.debug("  getFromRedisCache: Cache retrieved: {}", cache != null ? cache.getClass().getSimpleName() : "NULL");
                
                if (cache != null) {
                    logger.debug("  getFromRedisCache: Cache found, getting value...");
                    logger.debug("  getFromRedisCache: About to call cache.get({})", key);
                    var value = cache.get(key);
                    logger.debug("  getFromRedisCache: Value retrieved: {}", value != null ? value.getClass().getSimpleName() : "NULL");
                    
                    if (value != null && value.get() instanceof Map) {
                        Map<String, Object> result = (Map<String, Object>) value.get();
                        logger.debug("  getFromRedisCache: ✅ Found value in Redis cache, size: {}", result.size());
                        return result;
                    } else {
                        logger.debug("  getFromRedisCache: ❌ No value found or value is not a Map");
                    }
                } else {
                    logger.warn("  getFromRedisCache: ❌ Cache '{}' not found in redisCacheManager", cacheName);
                }
            } catch (Exception e) {
                logger.error("  getFromRedisCache: ❌ Error getting from Redis cache: {}", e.getMessage(), e);
            }
            logger.debug("  getFromRedisCache: Returning null");
            return null;
        })
        .timeout(java.time.Duration.ofSeconds(2)) // Reduzir timeout para 2 segundos
        .doOnError(error -> {
            logger.error("  getFromRedisCache: ❌ Timeout or error in Redis operation: {}", error.getMessage());
        })
        .doOnSuccess(result -> {
            logger.debug("  getFromRedisCache: ✅ Operation completed successfully, result: {}", result != null ? "FOUND" : "NOT_FOUND");
        })
        .onErrorResume(error -> {
            logger.error("  getFromRedisCache: ❌ Resuming from error: {}", error.getMessage());
            return Mono.just(null); // Retornar null em vez de empty
        });
        */
    }
    
    private void putInLocalCache(String key, Map<String, Object> value) {
        logger.debug("  putInLocalCache: Storing key: {}, value size: {}", key, value != null ? value.size() : 0);
        try {
            // Determine which cache to use based on the key
            String cacheName = key.contains("-forecast-") ? 
                WeatherServiceConstants.CACHE_WEATHER_FORECAST : 
                WeatherServiceConstants.CACHE_WEATHER_CURRENT;
            
            logger.debug("  putInLocalCache: Using cache name: {}", cacheName);
            
            var cache = localCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                logger.debug("  putInLocalCache: ✅ Successfully stored in local cache");
            } else {
                logger.warn("  putInLocalCache: ❌ Cache '{}' not found in localCacheManager", cacheName);
            }
        } catch (Exception e) {
            logger.error("  putInLocalCache: ❌ Error putting in local cache: {}", e.getMessage(), e);
        }
    }
    
    private void putInRedisCache(String key, Map<String, Object> value) {
        logger.debug("  putInRedisCache: Storing key: {}, value size: {}", key, value != null ? value.size() : 0);
        try {
            // Determine which cache to use based on the key
            String cacheName = key.contains("-forecast-") ? 
                WeatherServiceConstants.CACHE_WEATHER_FORECAST : 
                WeatherServiceConstants.CACHE_WEATHER_CURRENT;
            
            logger.debug("  putInRedisCache: Using cache name: {}", cacheName);
            
            var cache = redisCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                logger.debug("  putInRedisCache: ✅ Successfully stored in Redis cache");
            } else {
                logger.warn("  putInRedisCache: ❌ Cache '{}' not found in redisCacheManager", cacheName);
            }
        } catch (Exception e) {
            logger.error("  putInRedisCache: ❌ Error putting in Redis cache: {}", e.getMessage(), e);
        }
    }
} 