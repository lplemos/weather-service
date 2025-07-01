package com.lplemos.weather_service.service;

/**
 * Constants for WeatherService operations
 */
public final class WeatherServiceConstants {
    
    private WeatherServiceConstants() {
        // Utility class - prevent instantiation
    }
    
    // Cache names
    public static final String CACHE_WEATHER_CURRENT = "weather-current";
    public static final String CACHE_WEATHER_FORECAST = "weather-forecast";
    public static final String CACHE_CITY_INFO = "city-info";
    
    // Cache TTLs (in seconds)
    public static final long CACHE_TTL_CURRENT_WEATHER = 600; // 10 minutes
    public static final long CACHE_TTL_FORECAST = 3600; // 1 hour
    public static final long CACHE_TTL_CITY_INFO = 86400; // 24 hours
    
    // Log Messages
    public static final String LOG_PREFIX = "[WeatherService]";
    public static final String LOG_PROVIDER_SELECTED = "{} Selected provider: {} for city: {}";
    public static final String LOG_PROVIDER_NOT_FOUND = "{} Provider not found: {}";
    public static final String LOG_PROVIDER_UNAVAILABLE = "{} Provider unavailable: {}";
    public static final String LOG_FALLBACK_PROVIDER = "{} Using fallback provider: {}";
    public static final String LOG_NO_PROVIDERS_AVAILABLE = "{} No weather providers available";
    public static final String LOG_ERROR_PROCESSING = "{} Error processing request for {}: {}";
    public static final String LOG_FETCHING_WEATHER = "Fetching weather data for city: {}";
    public static final String LOG_FETCHING_FORECAST = "Fetching forecast data for city: {}";
    public static final String LOG_CACHE_HIT = "Cache hit for key: {}";
    public static final String LOG_CACHE_MISS = "Cache miss for key: {}";
    public static final String LOG_CACHE_EVICT = "Evicting cache for key: {}";
    public static final String LOG_API_RATE_LIMIT = "API rate limit info - Remaining: {}, Reset: {}";
    
    // Default Values
    public static final String DEFAULT_PROVIDER = "openweathermap";
    public static final String DEFAULT_CITY = "Lisbon";
    
    // Error Messages
    public static final String ERROR_NO_PROVIDERS = "No weather providers available";
    public static final String ERROR_PROVIDER_NOT_FOUND = "Weather provider not found: %s";
    public static final String ERROR_PROVIDER_UNAVAILABLE = "Weather provider unavailable: %s";
} 