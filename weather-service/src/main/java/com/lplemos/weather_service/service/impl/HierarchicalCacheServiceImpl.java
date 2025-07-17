package com.lplemos.weather_service.service.impl;

import com.lplemos.weather_service.exception.InvalidRequestException;
import com.lplemos.weather_service.model.WeatherProviderType;
import com.lplemos.weather_service.service.HierarchicalCacheService;
import com.lplemos.weather_service.service.WeatherService;
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
        logger.info("- LocalCacheManager: {}", localCacheManager != null ? localCacheManager.getClass().getSimpleName() : "NULL");
        logger.info("- RedisCacheManager: {}", redisCacheManager != null ? redisCacheManager.getClass().getSimpleName() : "NULL");
        logger.info("- RedisTemplate: {}", redisTemplate != null ? redisTemplate.getClass().getSimpleName() : "NULL");
        logger.info("- WeatherService: {}", weatherService != null ? weatherService.getClass().getSimpleName() : "NULL");
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName, String providerType, String language) {
        // Validate input parameters
        if (cityName == null || cityName.trim().isEmpty()) {
            return Mono.error(new InvalidRequestException("City name cannot be empty"));
        }
        
        if (!cityName.matches("^[a-zA-ZÀ-ÿ\\s\\-',]+$")) {
            return Mono.error(new InvalidRequestException("City name can only contain letters, spaces, hyphens, apostrophes, and commas"));
        }
        
        String cacheKey = cityName + "-current-" + providerType + "-" + language;
        logger.info("=== HierarchicalCache.getCurrentWeather START ===");
        logger.info("City: {} | Provider: {} | Language: {} | CacheKey: {}", cityName, providerType, language, cacheKey);
        
        if (weatherService == null) {
            logger.error("HierarchicalCache: WeatherService is null!");
            return Mono.error(new IllegalStateException("WeatherService not injected"));
        }
        
        return Mono.defer(() -> {
            Map<String, Object> localResult = getFromLocalCache(cacheKey);
            if (localResult != null) {
                logger.info("Local cache HIT for key: {}", cacheKey);
                return Mono.just(localResult);
            }
            logger.info("Local cache MISS for key: {}", cacheKey);
            
            return getFromRedisCache(cacheKey)
                    .flatMap(redisResult -> {
                        if (redisResult != null) {
                            logger.info("Redis cache HIT for key: {}", cacheKey);
                            putInLocalCache(cacheKey, redisResult);
                            return Mono.just(redisResult);
                        }
                        logger.info("Redis cache MISS for key: {}", cacheKey);
                        logger.info("Calling external API for city: {}", cityName);
                        WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
                        return weatherService.getCurrentWeather(cityName, providerTypeEnum, language)
                                .flatMap(apiResult -> {
                                    logger.info("API call SUCCESS for city: {}", cityName);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("API call FAILED for city: {}", cityName, error);
                                });
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        logger.info("Redis cache returned empty, calling external API for city: {}", cityName);
                        WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
                        return weatherService.getCurrentWeather(cityName, providerTypeEnum, language)
                                .flatMap(apiResult -> {
                                    logger.info("API call SUCCESS for city: {}", cityName);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("API call FAILED for city: {}", cityName, error);
                                });
                    }));
        });
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, String providerType, String language) {
        if (lat == null || lon == null) {
            return Mono.error(new InvalidRequestException("Latitude and longitude cannot be null"));
        }
        
        if (lat < -90 || lat > 90) {
            return Mono.error(new InvalidRequestException("Latitude must be between -90 and 90"));
        }
        
        if (lon < -180 || lon > 180) {
            return Mono.error(new InvalidRequestException("Longitude must be between -180 and 180"));
        }
        
        String cacheKey = String.format("coords-%.6f,%.6f-current-%s-%s", lat, lon, providerType, language);
        logger.info("=== HierarchicalCache.getCurrentWeatherByCoords START ===");
        logger.info("Coords: ({}, {}) | Provider: {} | Language: {} | CacheKey: {}", lat, lon, providerType, language, cacheKey);
        
        if (weatherService == null) {
            logger.error("HierarchicalCache: WeatherService is null!");
            return Mono.error(new IllegalStateException("WeatherService not injected"));
        }
        
        return Mono.defer(() -> {
            Map<String, Object> localResult = getFromLocalCache(cacheKey);
            if (localResult != null) {
                logger.info("Local cache HIT for key: {}", cacheKey);
                return Mono.just(localResult);
            }
            logger.info("Local cache MISS for key: {}", cacheKey);
            
            return getFromRedisCache(cacheKey)
                    .flatMap(redisResult -> {
                        if (redisResult != null) {
                            logger.info("Redis cache HIT for key: {}", cacheKey);
                            putInLocalCache(cacheKey, redisResult);
                            return Mono.just(redisResult);
                        }
                        logger.info("Redis cache MISS for key: {}", cacheKey);
                        logger.info("Calling external API for coords: ({}, {})", lat, lon);
                        WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
                        return weatherService.getCurrentWeatherByCoords(lat, lon, providerTypeEnum, language)
                                .flatMap(apiResult -> {
                                    logger.info("API call SUCCESS for coords: ({}, {})", lat, lon);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("API call FAILED for coords: ({}, {})", lat, lon, error);
                                });
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        logger.info("Redis cache returned empty, calling external API for coords: ({}, {})", lat, lon);
                        WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
                        return weatherService.getCurrentWeatherByCoords(lat, lon, providerTypeEnum, language)
                                .flatMap(apiResult -> {
                                    logger.info("API call SUCCESS for coords: ({}, {})", lat, lon);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("API call FAILED for coords: ({}, {})", lat, lon, error);
                                });
                    }));
        });
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName, String providerType, String language) {
        // Validate input parameters
        if (cityName == null || cityName.trim().isEmpty()) {
            return Mono.error(new InvalidRequestException("City name cannot be empty"));
        }
        
        if (!cityName.matches("^[a-zA-ZÀ-ÿ\\s\\-',]+$")) {
            return Mono.error(new InvalidRequestException("City name can only contain letters, spaces, hyphens, apostrophes, and commas"));
        }
        
        String cacheKey = cityName + "-forecast-" + providerType + "-" + language;
        logger.info("=== HierarchicalCache.getWeatherForecast START ===");
        logger.info("City: {} | Provider: {} | Language: {} | CacheKey: {}", cityName, providerType, language, cacheKey);
        
        return Mono.defer(() -> {
            Map<String, Object> localResult = getFromLocalCache(cacheKey);
            if (localResult != null) {
                logger.info("Local cache HIT for key: {}", cacheKey);
                return Mono.just(localResult);
            }
            logger.info("Local cache MISS for key: {}", cacheKey);
            
            return getFromRedisCache(cacheKey)
                    .flatMap(redisResult -> {
                        if (redisResult != null) {
                            logger.info("Redis cache HIT for key: {}", cacheKey);
                            putInLocalCache(cacheKey, redisResult);
                            return Mono.just(redisResult);
                        }
                        logger.info("Redis cache MISS for key: {}", cacheKey);
                        logger.info("Calling external API for city: {}", cityName);
                        WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
                        return weatherService.getWeatherForecast(cityName, providerTypeEnum, language)
                                .flatMap(apiResult -> {
                                    logger.info("API call SUCCESS for city: {}", cityName);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("API call FAILED for city: {}", cityName, error);
                                });
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        logger.info("Redis cache returned empty, calling external API for city: {}", cityName);
                        WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
                        return weatherService.getWeatherForecast(cityName, providerTypeEnum, language)
                                .flatMap(apiResult -> {
                                    logger.info("API call SUCCESS for city: {}", cityName);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("API call FAILED for city: {}", cityName, error);
                                });
                    }));
        });
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, String providerType, String language) {
        if (lat == null || lon == null) {
            return Mono.error(new InvalidRequestException("Latitude and longitude cannot be null"));
        }
        
        if (lat < -90 || lat > 90) {
            return Mono.error(new InvalidRequestException("Latitude must be between -90 and 90"));
        }
        
        if (lon < -180 || lon > 180) {
            return Mono.error(new InvalidRequestException("Longitude must be between -180 and 180"));
        }
        
        String cacheKey = String.format("coords-%.6f,%.6f-forecast-%s-%s", lat, lon, providerType, language);
        logger.info("=== HierarchicalCache.getWeatherForecastByCoords START ===");
        logger.info("Coords: ({}, {}) | Provider: {} | Language: {} | CacheKey: {}", lat, lon, providerType, language, cacheKey);
        
        if (weatherService == null) {
            logger.error("HierarchicalCache: WeatherService is null!");
            return Mono.error(new IllegalStateException("WeatherService not injected"));
        }
        
        return Mono.defer(() -> {
            Map<String, Object> localResult = getFromLocalCache(cacheKey);
            if (localResult != null) {
                logger.info("Local cache HIT for key: {}", cacheKey);
                return Mono.just(localResult);
            }
            logger.info("Local cache MISS for key: {}", cacheKey);
            
            return getFromRedisCache(cacheKey)
                    .flatMap(redisResult -> {
                        if (redisResult != null) {
                            logger.info("Redis cache HIT for key: {}", cacheKey);
                            putInLocalCache(cacheKey, redisResult);
                            return Mono.just(redisResult);
                        }
                        logger.info("Redis cache MISS for key: {}", cacheKey);
                        logger.info("Calling external API for coords: ({}, {})", lat, lon);
                        WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
                        return weatherService.getWeatherForecastByCoords(lat, lon, providerTypeEnum, language)
                                .flatMap(apiResult -> {
                                    logger.info("API call SUCCESS for coords: ({}, {})", lat, lon);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("API call FAILED for coords: ({}, {})", lat, lon, error);
                                });
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        logger.info("Redis cache returned empty, calling external API for coords: ({}, {})", lat, lon);
                        WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
                        return weatherService.getWeatherForecastByCoords(lat, lon, providerTypeEnum, language)
                                .flatMap(apiResult -> {
                                    logger.info("API call SUCCESS for coords: ({}, {})", lat, lon);
                                    putInLocalCache(cacheKey, apiResult);
                                    putInRedisCache(cacheKey, apiResult);
                                    return Mono.just(apiResult);
                                })
                                .doOnError(error -> {
                                    logger.error("API call FAILED for coords: ({}, {})", lat, lon, error);
                                });
                    }));
        });
    }
    
    @Override
    public Mono<Boolean> evictCityCache(String cityName) {
        return Mono.fromCallable(() -> {
            try {
                // Evict from local cache
                org.springframework.cache.Cache localCurrentCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
                org.springframework.cache.Cache localForecastCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
                
                if (localCurrentCache != null) {
                    localCurrentCache.evictIfPresent(cityName + "-current-OPENWEATHERMAP");
                }
                if (localForecastCache != null) {
                    localForecastCache.evictIfPresent(cityName + "-forecast-OPENWEATHERMAP");
                }
                
                // Evict from Redis cache
                org.springframework.cache.Cache redisCurrentCache = redisCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
                org.springframework.cache.Cache redisForecastCache = redisCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
                
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
                    org.springframework.cache.Cache cache = localCacheManager.getCache(name);
                    if (cache != null) {
                        cache.clear();
                    }
                });
                
                // Clear Redis caches
                redisCacheManager.getCacheNames().forEach(name -> {
                    org.springframework.cache.Cache cache = redisCacheManager.getCache(name);
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
                logger.info("Getting local cache stats...");
                // Local cache stats (Custom TTL Cache)
                org.springframework.cache.Cache localCurrentCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
                org.springframework.cache.Cache localForecastCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
                
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
                
                logger.info("Getting Redis info...");
                // Redis info
                java.util.Properties redisInfo = redisTemplate.getConnectionFactory().getConnection().serverCommands().info();
                stats.put("redisInfo", redisInfo);
                logger.info("Redis info retrieved");
                
                stats.put("cacheAvailable", true);
                logger.info("=== HierarchicalCache.getCacheStats END (SUCCESS) ===");
                
            } catch (Exception e) {
                logger.error("Error getting cache stats: {}", e.getMessage(), e);
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
            
            logger.info("=== HierarchicalCache.getCacheHealth END ===");
            return health;
        });
    }
    
    // Helper methods
    @SuppressWarnings("unchecked")
    private Map<String, Object> getFromLocalCache(String key) {
        logger.debug("getFromLocalCache: Checking key: {}", key);
        try {
            // Determine which cache to use based on the key
            String cacheName = key.contains("-forecast-") ? 
                WeatherServiceConstants.CACHE_WEATHER_FORECAST : 
                WeatherServiceConstants.CACHE_WEATHER_CURRENT;
            
            logger.debug("getFromLocalCache: Using cache name: {}", cacheName);
            
            org.springframework.cache.Cache cache = localCacheManager.getCache(cacheName);
            if (cache != null) {
                logger.debug("getFromLocalCache: Cache found, getting value...");
                org.springframework.cache.Cache.ValueWrapper value = cache.get(key);
                if (value != null && value.get() instanceof Map) {
                    Map<String, Object> result = (Map<String, Object>) value.get();
                    logger.debug("getFromLocalCache: Found value in local cache, size: {}", result.size());
                    return result;
                } else {
                    logger.debug("getFromLocalCache: No value found or value is not a Map");
                }
            } else {
                logger.warn("getFromLocalCache: Cache '{}' not found in localCacheManager", cacheName);
            }
        } catch (Exception e) {
            logger.error("getFromLocalCache: Error getting from local cache: {}", e.getMessage(), e);
        }
        logger.debug("getFromLocalCache: Returning null");
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Mono<Map<String, Object>> getFromRedisCache(String key) {
        logger.debug("getFromRedisCache: Checking key: {}", key);
        
        return Mono.fromCallable(() -> {
            logger.debug("getFromRedisCache: Starting fromCallable for key: {}", key);
            try {
                // Determine which cache to use based on the key
                String cacheName = key.contains("-forecast-") ? 
                    WeatherServiceConstants.CACHE_WEATHER_FORECAST : 
                    WeatherServiceConstants.CACHE_WEATHER_CURRENT;
                
                logger.debug("getFromRedisCache: Using cache name: {}", cacheName);
                
                org.springframework.cache.Cache cache = redisCacheManager.getCache(cacheName);
                logger.debug("getFromRedisCache: Cache retrieved: {}", cache != null ? cache.getClass().getSimpleName() : "NULL");
                
                if (cache != null) {
                    logger.debug("getFromRedisCache: Cache found, getting value...");
                    org.springframework.cache.Cache.ValueWrapper value = cache.get(key);
                    logger.debug("getFromRedisCache: Value retrieved: {}", value != null ? value.getClass().getSimpleName() : "NULL");
                    
                    if (value != null && value.get() instanceof Map) {
                        Map<String, Object> result = (Map<String, Object>) value.get();
                        logger.debug("getFromRedisCache: Found value in Redis cache, size: {}", result.size());
                        return result;
                    } else {
                        logger.debug("getFromRedisCache: No value found or value is not a Map");
                    }
                } else {
                    logger.warn("getFromRedisCache: Cache '{}' not found in redisCacheManager", cacheName);
                }
            } catch (Exception e) {
                logger.error("getFromRedisCache: Error getting from Redis cache: {}", e.getMessage(), e);
            }
            logger.debug("getFromRedisCache: Returning null");
            return null;
        })
        .doOnError(error -> {
            logger.error("getFromRedisCache: Error in Redis operation: {}", error.getMessage());
        })
        .onErrorResume(error -> {
            logger.error("getFromRedisCache: Resuming from error: {}", error.getMessage());
            return Mono.empty();
        });
    }
    
    private void putInLocalCache(String key, Map<String, Object> value) {
        logger.debug("putInLocalCache: Storing key: {}, value size: {}", key, value != null ? value.size() : 0);
        try {
            // Determine which cache to use based on the key
            String cacheName = key.contains("-forecast-") ? 
                WeatherServiceConstants.CACHE_WEATHER_FORECAST : 
                WeatherServiceConstants.CACHE_WEATHER_CURRENT;
            
            logger.debug("putInLocalCache: Using cache name: {}", cacheName);
            
            org.springframework.cache.Cache cache = localCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                logger.debug("putInLocalCache: Successfully stored in local cache");
            } else {
                logger.warn("putInLocalCache: Cache '{}' not found in localCacheManager", cacheName);
            }
        } catch (Exception e) {
            logger.error("putInLocalCache: Error putting in local cache: {}", e.getMessage(), e);
        }
    }
    
    private void putInRedisCache(String key, Map<String, Object> value) {
        logger.debug("putInRedisCache: Storing key: {}, value size: {}", key, value != null ? value.size() : 0);
        try {
            // Determine which cache to use based on the key
            String cacheName = key.contains("-forecast-") ? 
                WeatherServiceConstants.CACHE_WEATHER_FORECAST : 
                WeatherServiceConstants.CACHE_WEATHER_CURRENT;
            
            logger.debug("putInRedisCache: Using cache name: {}", cacheName);
            
            org.springframework.cache.Cache cache = redisCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                logger.debug("putInRedisCache: Successfully stored in Redis cache");
            } else {
                logger.warn("putInRedisCache: Cache '{}' not found in redisCacheManager", cacheName);
            }
        } catch (Exception e) {
            logger.error("putInRedisCache: Error putting in Redis cache: {}", e.getMessage(), e);
        }
    }
} 