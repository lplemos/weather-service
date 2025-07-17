package com.lplemos.weather_service.config;

import com.lplemos.weather_service.service.WeatherServiceConstants;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class LocalCacheConfig {

    /**
     * Custom Local Cache Manager with TTL support
     * Uses ConcurrentHashMap with scheduled cleanup
     */
    @Bean
    public CacheManager localCacheManager() {
        return new CacheManager() {
            private final ConcurrentHashMap<String, Cache> caches = new ConcurrentHashMap<>();
            private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

            {
                // Initialize caches
                caches.put(WeatherServiceConstants.CACHE_WEATHER_CURRENT, 
                    new TTLConcurrentMapCache(WeatherServiceConstants.CACHE_WEATHER_CURRENT, Duration.ofMinutes(5)));
                caches.put(WeatherServiceConstants.CACHE_WEATHER_FORECAST, 
                    new TTLConcurrentMapCache(WeatherServiceConstants.CACHE_WEATHER_FORECAST, Duration.ofMinutes(5)));
                
                // Schedule cleanup every 30 seconds
                cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 30, 30, TimeUnit.SECONDS);
            }

            @Override
            public Cache getCache(String name) {
                return caches.get(name);
            }

            @Override
            public java.util.Collection<String> getCacheNames() {
                return caches.keySet();
            }

            private void cleanupExpiredEntries() {
                caches.values().forEach(cache -> {
                    if (cache instanceof TTLConcurrentMapCache) {
                        ((TTLConcurrentMapCache) cache).cleanupExpiredEntries();
                    }
                });
            }
        };
    }

    /**
     * Custom Cache implementation with TTL support
     */
    private static class TTLConcurrentMapCache implements Cache {
        private final String name;
        private final Duration ttl;
        private final ConcurrentHashMap<Object, CacheEntry> store = new ConcurrentHashMap<>();

        public TTLConcurrentMapCache(String name, Duration ttl) {
            this.name = name;
            this.ttl = ttl;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return store;
        }

        @Override
        public ValueWrapper get(Object key) {
            CacheEntry entry = store.get(key);
            if (entry != null && !entry.isExpired()) {
                return () -> entry.value;
            }
            if (entry != null && entry.isExpired()) {
                store.remove(key);
            }
            return null;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            ValueWrapper wrapper = get(key);
            return wrapper != null ? (T) wrapper.get() : null;
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            ValueWrapper wrapper = get(key);
            if (wrapper != null) {
                return (T) wrapper.get();
            }
            
            try {
                T value = valueLoader.call();
                put(key, value);
                return value;
            } catch (Exception e) {
                throw new RuntimeException("Error loading value for key: " + key, e);
            }
        }

        @Override
        public void put(Object key, Object value) {
            store.put(key, new CacheEntry(value, System.currentTimeMillis() + ttl.toMillis()));
        }

        @Override
        public void evict(Object key) {
            store.remove(key);
        }

        @Override
        public void clear() {
            store.clear();
        }

        @Override
        public ValueWrapper putIfAbsent(Object key, Object value) {
            CacheEntry existing = store.putIfAbsent(key, new CacheEntry(value, System.currentTimeMillis() + ttl.toMillis()));
            return existing != null && !existing.isExpired() ? () -> existing.value : null;
        }

        public void cleanupExpiredEntries() {
            store.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }

        private static class CacheEntry {
            final Object value;
            final long expirationTime;

            CacheEntry(Object value, long expirationTime) {
                this.value = value;
                this.expirationTime = expirationTime;
            }

            boolean isExpired() {
                return System.currentTimeMillis() > expirationTime;
            }
        }
    }
} 