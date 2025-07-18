package com.lplemos.weather_service.service.impl;

import com.lplemos.weather_service.model.WeatherProviderType;
import com.lplemos.weather_service.service.HierarchicalCacheService;
import com.lplemos.weather_service.service.WeatherService;
import com.lplemos.weather_service.service.WeatherServiceConstants;
import com.lplemos.weather_service.validation.WeatherDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Supplier;

@Service
public class HierarchicalCacheServiceImpl implements HierarchicalCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(HierarchicalCacheServiceImpl.class);
    
    private final CacheManager localCacheManager;
    private final CacheManager redisCacheManager;
    private final WeatherService weatherService;
    private final WeatherDataValidator validator;
    
    public HierarchicalCacheServiceImpl(
            CacheManager localCacheManager,
            CacheManager redisCacheManager,
            WeatherService weatherService,
            WeatherDataValidator validator) {
        this.localCacheManager = localCacheManager;
        this.redisCacheManager = redisCacheManager;
        this.weatherService = weatherService;
        this.validator = validator;
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName, String providerType, String language) {
        validator.validateCityName(cityName);
        validator.validateWeatherService(weatherService);
        
        String cacheKey = generateCacheKey(cityName, "current", providerType, language);
        String identifier = "city: " + cityName;
        
        logger.info("=== HierarchicalCache.getCurrentWeather START ===");
        logger.info("City: {} | Provider: {} | Language: {} | CacheKey: {}", cityName, providerType, language, cacheKey);
        
        return getWeatherDataWithCaching(cacheKey, identifier, () -> {
            WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
            return weatherService.getCurrentWeather(cityName, providerTypeEnum, language);
        });
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, String providerType, String language) {
        validator.validateCoordinates(lat, lon);
        validator.validateWeatherService(weatherService);
        
        String cacheKey = generateCacheKeyByCoords(lat, lon, "current", providerType, language);
        String identifier = String.format("coords: (%.6f, %.6f)", lat, lon);
        
        logger.info("=== HierarchicalCache.getCurrentWeatherByCoords START ===");
        logger.info("Coords: ({}, {}) | Provider: {} | Language: {} | CacheKey: {}", lat, lon, providerType, language, cacheKey);
        
        return getWeatherDataWithCaching(cacheKey, identifier, () -> {
            WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
            return weatherService.getCurrentWeatherByCoords(lat, lon, providerTypeEnum, language);
        });
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName, String providerType, String language) {
        validator.validateCityName(cityName);
        validator.validateWeatherService(weatherService);
        
        String cacheKey = generateCacheKey(cityName, "forecast", providerType, language);
        String identifier = "city: " + cityName;
        
        logger.info("=== HierarchicalCache.getWeatherForecast START ===");
        logger.info("City: {} | Provider: {} | Language: {} | CacheKey: {}", cityName, providerType, language, cacheKey);
        
        return getWeatherDataWithCaching(cacheKey, identifier, () -> {
            WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
            return weatherService.getWeatherForecast(cityName, providerTypeEnum, language);
        });
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, String providerType, String language) {
        validator.validateCoordinates(lat, lon);
        validator.validateWeatherService(weatherService);
        
        String cacheKey = generateCacheKeyByCoords(lat, lon, "forecast", providerType, language);
        String identifier = String.format("coords: (%.6f, %.6f)", lat, lon);
        
        logger.info("=== HierarchicalCache.getWeatherForecastByCoords START ===");
        logger.info("Coords: ({}, {}) | Provider: {} | Language: {} | CacheKey: {}", lat, lon, providerType, language, cacheKey);
        
        return getWeatherDataWithCaching(cacheKey, identifier, () -> {
            WeatherProviderType providerTypeEnum = WeatherProviderType.fromCode(providerType);
            return weatherService.getWeatherForecastByCoords(lat, lon, providerTypeEnum, language);
        });
    }
    
    @Override
    public Mono<Boolean> evictCityCache(String cityName) {
        return Mono.fromCallable(() -> {
            try {
                evictFromLocalCache(cityName);
                evictFromRedisCache(cityName);
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
                clearAllLocalCaches();
                clearAllRedisCaches();
                logger.info(WeatherServiceConstants.LOG_CACHE_EVICT, "All cache");
                return true;
            } catch (Exception e) {
                logger.error("Error evicting all cache", e);
                return false;
            }
        });
    }
    
    private Mono<Map<String, Object>> getWeatherDataWithCaching(
            String cacheKey, 
            String identifier,
            Supplier<Mono<Map<String, Object>>> apiCallSupplier) {
        
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
                        logger.info("Calling external API for: {}", identifier);
                        
                        return callExternalApiAndCache(cacheKey, identifier, apiCallSupplier);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        logger.info("Redis cache returned empty, calling external API for: {}", identifier);
                        
                        return callExternalApiAndCache(cacheKey, identifier, apiCallSupplier);
                    }));
        });
    }
    
    private Mono<Map<String, Object>> callExternalApiAndCache(
            String cacheKey, 
            String identifier,
            Supplier<Mono<Map<String, Object>>> apiCallSupplier) {
        return apiCallSupplier.get()
                .flatMap(apiResult -> {
                    logger.info("API call SUCCESS for: {}", identifier);
                    putInLocalCache(cacheKey, apiResult);
                    putInRedisCache(cacheKey, apiResult);
                    return Mono.just(apiResult);
                })
                .doOnError(error -> {
                    logger.error("API call FAILED for: {}", identifier, error);
                });
    }
    
    private String generateCacheKey(String cityName, String type, String providerType, String language) {
        return cityName + "-" + type + "-" + providerType + "-" + language;
    }
    
    private String generateCacheKeyByCoords(Double lat, Double lon, String type, String providerType, String language) {
        return String.format("coords-%.6f,%.6f-%s-%s-%s", lat, lon, type, providerType, language);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getFromLocalCache(String key) {
        logger.debug("getFromLocalCache: Checking key: {}", key);
        try {
            String cacheName = getCacheNameFromKey(key);
            logger.debug("getFromLocalCache: Using cache name: {}", cacheName);
            
            Cache cache = localCacheManager.getCache(cacheName);
            if (cache != null) {
                logger.debug("getFromLocalCache: Cache found, getting value...");
                Cache.ValueWrapper value = cache.get(key);
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
                String cacheName = getCacheNameFromKey(key);
                logger.debug("getFromRedisCache: Using cache name: {}", cacheName);
                
                Cache cache = redisCacheManager.getCache(cacheName);
                logger.debug("getFromRedisCache: Cache retrieved: {}", cache != null ? cache.getClass().getSimpleName() : "NULL");
                
                if (cache != null) {
                    logger.debug("getFromRedisCache: Cache found, getting value...");
                    Cache.ValueWrapper value = cache.get(key);
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
            String cacheName = getCacheNameFromKey(key);
            logger.debug("putInLocalCache: Using cache name: {}", cacheName);
            
            Cache cache = localCacheManager.getCache(cacheName);
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
            String cacheName = getCacheNameFromKey(key);
            logger.debug("putInRedisCache: Using cache name: {}", cacheName);
            
            Cache cache = redisCacheManager.getCache(cacheName);
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
    
    private String getCacheNameFromKey(String key) {
        return key.contains("-forecast-") ? 
            WeatherServiceConstants.CACHE_WEATHER_FORECAST : 
            WeatherServiceConstants.CACHE_WEATHER_CURRENT;
    }
    
    private void evictFromLocalCache(String cityName) {
        org.springframework.cache.Cache localCurrentCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
        org.springframework.cache.Cache localForecastCache = localCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
        
        if (localCurrentCache != null) {
            localCurrentCache.evictIfPresent(cityName + "-current-OPENWEATHERMAP");
        }
        if (localForecastCache != null) {
            localForecastCache.evictIfPresent(cityName + "-forecast-OPENWEATHERMAP");
        }
    }
    
    private void evictFromRedisCache(String cityName) {
        org.springframework.cache.Cache redisCurrentCache = redisCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT);
        org.springframework.cache.Cache redisForecastCache = redisCacheManager.getCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST);
        
        if (redisCurrentCache != null) {
            redisCurrentCache.evictIfPresent(cityName + "-current-OPENWEATHERMAP");
        }
        if (redisForecastCache != null) {
            redisForecastCache.evictIfPresent(cityName + "-forecast-OPENWEATHERMAP");
        }
    }
    
    private void clearAllLocalCaches() {
        localCacheManager.getCacheNames().forEach(name -> {
            org.springframework.cache.Cache cache = localCacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }
    
    private void clearAllRedisCaches() {
        redisCacheManager.getCacheNames().forEach(name -> {
            org.springframework.cache.Cache cache = redisCacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }
    

} 