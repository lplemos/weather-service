package com.lplemos.weather_service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.lplemos.weather_service.service.WeatherServiceConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class HierarchicalCacheConfig {

    /**
     * Local Cache Manager (Caffeine) - Primary for fast access
     */
    @Bean
    @Primary
    public CacheManager localCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Enable async cache mode for WebFlux compatibility
        cacheManager.setAsyncCacheMode(true);
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        
        // Configure specific caches com TTL de 10 minutos
        cacheManager.registerCustomCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT,
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
        
        cacheManager.registerCustomCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST,
                Caffeine.newBuilder()
                        .maximumSize(200)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
        
        return cacheManager;
    }

    /**
     * Redis Cache Manager (Distributed) - Secondary for persistence
     * TTL: 10-60 minutes
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL de 10 minutos
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration(WeatherServiceConstants.CACHE_WEATHER_CURRENT, 
                    config.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(WeatherServiceConstants.CACHE_WEATHER_FORECAST, 
                    config.entryTtl(Duration.ofMinutes(10)))
                .build();
    }
} 