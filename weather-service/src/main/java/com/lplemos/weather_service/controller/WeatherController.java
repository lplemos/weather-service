package com.lplemos.weather_service.controller;

import com.lplemos.weather_service.controller.constants.ApiVersionConstants;
import com.lplemos.weather_service.controller.constants.WeatherControllerConstants;
import com.lplemos.weather_service.model.WeatherProviderType;
import com.lplemos.weather_service.model.WeatherResponse;
import com.lplemos.weather_service.model.WeatherSummary;
import com.lplemos.weather_service.service.CacheService;
import com.lplemos.weather_service.service.HierarchicalCacheService;
import com.lplemos.weather_service.service.WeatherService;
import com.lplemos.weather_service.validation.ValidProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Weather Controller with API versioning support
 * Supports both versioned and non-versioned endpoints
 */
@RestController
@RequestMapping(WeatherControllerConstants.BASE_PATH)
@Valid
public class WeatherController {
    
    private final WeatherService weatherService;
    private final CacheService cacheService;
    private final HierarchicalCacheService hierarchicalCacheService;
    
    public WeatherController(WeatherService weatherService, CacheService cacheService, HierarchicalCacheService hierarchicalCacheService) {
        this.weatherService = weatherService;
        this.cacheService = cacheService;
        this.hierarchicalCacheService = hierarchicalCacheService;
    }
    
    /**
     * Test endpoint to verify OpenWeatherMap API key is working
     * GET /api/v1/weather/test?city=Coimbra
     */
    @GetMapping(WeatherControllerConstants.TEST_ENDPOINT)
    public Mono<Map<String, Object>> testWeatherApi(
            @RequestParam(defaultValue = WeatherControllerConstants.DEFAULT_TEST_CITY)
            @Valid
            @NotBlank(message = "City name cannot be empty")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-']+$", message = "City name can only contain letters, spaces, hyphens, and apostrophes")
            String city,
            @RequestParam(value = "lang", required = false, defaultValue = "en")
            @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a 2-letter code (e.g., en, pt, es)")
            String language) {
        return hierarchicalCacheService.getCurrentWeather(city, "OPENWEATHERMAP", language);
    }
    
    /**
     * Get current weather for a city (raw response)
     * GET /api/v1/weather/current?city=Lisbon
     * GET /api/v1/weather/current?city=Lisbon&provider=openweathermap&lang=pt
     */
    @GetMapping(WeatherControllerConstants.CURRENT_ENDPOINT)
    public Mono<Map<String, Object>> getCurrentWeather(
            @RequestParam(value = WeatherControllerConstants.PARAM_CITY, required = false) 
            @Valid
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-']*$", message = "City name can only contain letters, spaces, hyphens, and apostrophes")
            String city,
            @RequestParam(value = "lat", required = false)
            @Valid
            @Min(value = -90, message = "Latitude must be between -90 and 90")
            @Max(value = 90, message = "Latitude must be between -90 and 90")
            Double lat,
            @RequestParam(value = "lon", required = false)
            @Valid
            @Min(value = -180, message = "Longitude must be between -180 and 180")
            @Max(value = 180, message = "Longitude must be between -180 and 180")
            Double lon,
            @RequestParam(value = WeatherControllerConstants.PARAM_PROVIDER, required = false)
            @ValidProvider
            String provider,
            @RequestParam(value = "lang", required = false, defaultValue = "en")
            @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a 2-letter code (e.g., en, pt, es)")
            String language) {
        
        // Validate that either city or coordinates are provided
        if ((city == null || city.trim().isEmpty()) && (lat == null || lon == null)) {
            return Mono.error(new IllegalArgumentException("Either city name or coordinates (lat, lon) must be provided"));
        }
        
        if (city != null && !city.trim().isEmpty() && (lat != null || lon != null)) {
            return Mono.error(new IllegalArgumentException("Cannot provide both city name and coordinates"));
        }
        
        String providerType = provider != null ? provider : "OPENWEATHERMAP";
        
        if (lat != null && lon != null) {
            return hierarchicalCacheService.getCurrentWeatherByCoords(lat, lon, providerType, language);
        } else {
            return hierarchicalCacheService.getCurrentWeather(city, providerType, language);
        }
    }
    
    /**
     * Get current weather for a city (structured response)
     * GET /api/v1/weather/current/structured?city=Porto
     * GET /api/v1/weather/current/structured?city=Porto&provider=openweathermap
     */
    @GetMapping(WeatherControllerConstants.CURRENT_STRUCTURED_ENDPOINT)
    public Mono<WeatherResponse> getCurrentWeatherStructured(
            @RequestParam(WeatherControllerConstants.PARAM_CITY) 
            @Valid
            @NotBlank(message = "City name cannot be empty")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-']+$", message = "City name can only contain letters, spaces, hyphens, and apostrophes")
            String city,
            @RequestParam(value = WeatherControllerConstants.PARAM_PROVIDER, required = false)
            @ValidProvider
            String provider) {
        
        if (provider != null) {
            WeatherProviderType providerType = WeatherProviderType.fromCode(provider);
            return weatherService.getCurrentWeatherStructured(city, providerType);
        }
        return weatherService.getCurrentWeatherStructured(city);
    }
    
    /**
     * Get weather summary for a city (simplified response)
     * GET /api/v1/weather/summary?city=Coimbra
     * GET /api/v1/weather/summary?city=Coimbra&provider=openweathermap
     */
    @GetMapping(WeatherControllerConstants.SUMMARY_ENDPOINT)
    public Mono<WeatherSummary> getWeatherSummary(
            @RequestParam(WeatherControllerConstants.PARAM_CITY) 
            @Valid
            @NotBlank(message = "City name cannot be empty")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-']+$", message = "City name can only contain letters, spaces, hyphens, and apostrophes")
            String city,
            @RequestParam(value = WeatherControllerConstants.PARAM_PROVIDER, required = false)
            @ValidProvider
            String provider) {
        
        if (provider != null) {
            WeatherProviderType providerType = WeatherProviderType.fromCode(provider);
            return weatherService.getWeatherSummary(city, providerType);
        }
        return weatherService.getWeatherSummary(city);
    }
    
    /**
     * Get 5-day weather forecast for a city
     * GET /api/v1/weather/forecast?city=Porto
     * GET /api/v1/weather/forecast?city=Porto&provider=openweathermap&lang=pt
     */
    @GetMapping(WeatherControllerConstants.FORECAST_ENDPOINT)
    public Mono<Map<String, Object>> getWeatherForecast(
            @RequestParam(value = WeatherControllerConstants.PARAM_CITY, required = false) 
            @Valid
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-']*$", message = "City name can only contain letters, spaces, hyphens, and apostrophes")
            String city,
            @RequestParam(value = "lat", required = false)
            @Valid
            @Min(value = -90, message = "Latitude must be between -90 and 90")
            @Max(value = 90, message = "Latitude must be between -90 and 90")
            Double lat,
            @RequestParam(value = "lon", required = false)
            @Valid
            @Min(value = -180, message = "Longitude must be between -180 and 180")
            @Max(value = 180, message = "Longitude must be between -180 and 180")
            Double lon,
            @RequestParam(value = WeatherControllerConstants.PARAM_PROVIDER, required = false)
            @ValidProvider
            String provider,
            @RequestParam(value = "lang", required = false, defaultValue = "en")
            @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a 2-letter code (e.g., en, pt, es)")
            String language) {
        
        // Validate that either city or coordinates are provided
        if ((city == null || city.trim().isEmpty()) && (lat == null || lon == null)) {
            return Mono.error(new IllegalArgumentException("Either city name or coordinates (lat, lon) must be provided"));
        }
        
        if (city != null && !city.trim().isEmpty() && (lat != null || lon != null)) {
            return Mono.error(new IllegalArgumentException("Cannot provide both city name and coordinates"));
        }
        
        String providerType = provider != null ? provider : "OPENWEATHERMAP";
        
        if (lat != null && lon != null) {
            return hierarchicalCacheService.getWeatherForecastByCoords(lat, lon, providerType, language);
        } else {
            return hierarchicalCacheService.getWeatherForecast(city, providerType, language);
        }
    }
    
    /**
     * Get current weather by city ID (more precise)
     * GET /api/v1/weather/current/2742032 (Coimbra city ID)
     * GET /api/v1/weather/current/2742032?provider=openweathermap
     */
    @GetMapping(WeatherControllerConstants.CURRENT_BY_ID_ENDPOINT)
    public Mono<Map<String, Object>> getCurrentWeatherById(
            @PathVariable(WeatherControllerConstants.PATH_VAR_CITY_ID) 
            @Min(value = 1, message = "City ID must be a positive number")
            Integer cityId,
            @RequestParam(value = WeatherControllerConstants.PARAM_PROVIDER, required = false)
            @ValidProvider
            String provider) {
        
        if (provider != null) {
            WeatherProviderType providerType = WeatherProviderType.fromCode(provider);
            return weatherService.getCurrentWeatherById(cityId, providerType);
        }
        return weatherService.getCurrentWeatherById(cityId);
    }
    
    /**
     * Get available weather providers
     * GET /api/v1/weather/providers
     */
    @GetMapping(WeatherControllerConstants.PROVIDERS_ENDPOINT)
    public List<String> getAvailableProviders() {
        return weatherService.getAvailableProviders();
    }
    
    /**
     * Check if a specific provider is available
     * GET /api/v1/weather/providers/openweathermap/health
     */
    @GetMapping(WeatherControllerConstants.PROVIDER_HEALTH_ENDPOINT)
    public Mono<Boolean> isProviderAvailable(
            @PathVariable(WeatherControllerConstants.PATH_VAR_PROVIDER) 
            @ValidProvider
            String provider) {
        WeatherProviderType providerType = WeatherProviderType.fromCode(provider);
        return weatherService.isProviderAvailable(providerType);
    }
    
    /**
     * API Version information endpoint
     * GET /api/v1/weather/version
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> getApiVersion() {
        Map<String, Object> versionInfo = Map.of(
            "currentVersion", ApiVersionConstants.CURRENT_VERSION,
            "supportedVersions", List.of(ApiVersionConstants.V1),
            "basePath", ApiVersionConstants.CURRENT_BASE_PATH,
            "deprecated", false,
            "message", "API v1 is the current stable version."
        );
        return ResponseEntity.ok(versionInfo);
    }
    
    /**
     * Check API status and rate limiting information
     * GET /api/v1/weather/status
     */
    @GetMapping("/status")
    public Mono<Map<String, Object>> getApiStatus() {
        return weatherService.getDefaultProvider().isAvailable()
                .map(available -> Map.of(
                    "status", available ? "UP" : "DOWN",
                    "provider", weatherService.getDefaultProvider().getProviderName(),
                    "message", available ? "API is responding normally" : "API is not responding",
                    "timestamp", System.currentTimeMillis(),
                    "note", "Check logs for detailed rate limiting headers"
                ));
    }
    
    /**
     * Debug endpoint to show API configuration
     * GET /api/v1/weather/debug/config
     */
    @GetMapping("/debug/config")
    public Mono<Map<String, Object>> debugConfig() {
        return Mono.just(Map.<String, Object>of(
            "message", "API configuration debug",
            "timestamp", System.currentTimeMillis(),
            "note", "Check application logs for configuration details and URL construction"
        ));
    }
    
    /**
     * Get cache statistics
     * GET /api/v1/weather/cache/stats
     */
    @GetMapping("/cache/stats")
    public Mono<Map<String, Object>> getCacheStats() {
        return cacheService.getCacheStats();
    }
    
    /**
     * Get cache health status
     * GET /api/v1/weather/cache/health
     */
    @GetMapping("/cache/health")
    public Mono<Map<String, Object>> getCacheHealth() {
        return cacheService.isCacheAvailable()
                .map(available -> Map.<String, Object>of(
                    "cacheAvailable", available,
                    "status", available ? "UP" : "DOWN",
                    "message", available ? "Cache is responding normally" : "Cache is not available",
                    "timestamp", System.currentTimeMillis()
                ));
    }
    
    /**
     * Evict cache for a specific city
     * DELETE /api/v1/weather/cache/city?city=Lisbon
     */
    @DeleteMapping("/cache/city")
    public Mono<Map<String, Object>> evictCityCache(
            @RequestParam(WeatherControllerConstants.PARAM_CITY) 
            @NotBlank(message = "City name cannot be empty")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-']+$", message = "City name can only contain letters, spaces, hyphens, and apostrophes")
            String city) {
        return cacheService.evictCityCache(city)
                .map(success -> Map.<String, Object>of(
                    "success", success,
                    "city", city,
                    "message", success ? "Cache evicted successfully" : "Failed to evict cache",
                    "timestamp", System.currentTimeMillis()
                ));
    }
    
    /**
     * Evict all weather cache
     * DELETE /api/v1/weather/cache/all
     */
    @DeleteMapping("/cache/all")
    public Mono<Map<String, Object>> evictAllCache() {
        return cacheService.evictAllWeatherCache()
                .map(success -> Map.<String, Object>of(
                    "success", success,
                    "message", success ? "All weather cache evicted successfully" : "Failed to evict all cache",
                    "timestamp", System.currentTimeMillis()
                ));
    }
    
    /**
     * Get current weather (hierarchical) for a city
     * GET /api/v1/weather/hierarchical/current?city=Lisbon&provider=openweathermap&lang=pt
     */
    @GetMapping("/hierarchical/current")
    public Mono<Map<String, Object>> getCurrentWeatherHierarchical(
            @RequestParam(WeatherControllerConstants.PARAM_CITY)
            @Valid
            @NotBlank(message = "City name cannot be empty")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-']+$", message = "City name can only contain letters, spaces, hyphens, and apostrophes")
            String city,
            @RequestParam(value = WeatherControllerConstants.PARAM_PROVIDER, defaultValue = "OPENWEATHERMAP")
            @ValidProvider
            String provider,
            @RequestParam(value = "lang", required = false, defaultValue = "en")
            @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a 2-letter code (e.g., en, pt, es)")
            String language) {
        return hierarchicalCacheService.getCurrentWeather(city, provider, language);
    }

    /**
     * Get weather forecast (hierarchical) for a city
     * GET /api/v1/weather/hierarchical/forecast?city=Lisbon&provider=openweathermap&lang=pt
     */
    @GetMapping("/hierarchical/forecast")
    public Mono<Map<String, Object>> getWeatherForecastHierarchical(
            @RequestParam(WeatherControllerConstants.PARAM_CITY)
            @NotBlank(message = "City name cannot be empty")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-']+$", message = "City name can only contain letters, spaces, hyphens, and apostrophes")
            String city,
            @RequestParam(value = WeatherControllerConstants.PARAM_PROVIDER, defaultValue = "OPENWEATHERMAP")
            @ValidProvider
            String provider,
            @RequestParam(value = "lang", required = false, defaultValue = "en")
            @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a 2-letter code (e.g., en, pt, es)")
            String language) {
        return hierarchicalCacheService.getWeatherForecast(city, provider, language);
    }
    
    /**
     * Get hierarchical cache statistics
     * GET /api/v1/weather/hierarchical/cache/stats
     */
    @GetMapping("/hierarchical/cache/stats")
    public Mono<Map<String, Object>> getHierarchicalCacheStats() {
        return hierarchicalCacheService.getCacheStats();
    }
    
    /**
     * Get hierarchical cache health status
     * GET /api/v1/weather/hierarchical/cache/health
     */
    @GetMapping("/hierarchical/cache/health")
    public Mono<Map<String, Boolean>> getHierarchicalCacheHealth() {
        return hierarchicalCacheService.getCacheHealth();
    }
    
    /**
     * Evict hierarchical cache for a specific city
     * DELETE /api/v1/weather/hierarchical/cache/city?city=Lisbon
     */
    @DeleteMapping("/hierarchical/cache/city")
    public Mono<Map<String, Object>> evictHierarchicalCityCache(
            @RequestParam(WeatherControllerConstants.PARAM_CITY) 
            @NotBlank(message = "City name cannot be empty")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-']+$", message = "City name can only contain letters, spaces, hyphens, and apostrophes")
            String city) {
        return hierarchicalCacheService.evictCityCache(city)
                .map(success -> Map.<String, Object>of(
                    "success", success,
                    "city", city,
                    "message", success ? "Hierarchical cache evicted successfully" : "Failed to evict hierarchical cache",
                    "timestamp", System.currentTimeMillis()
                ));
    }
    
    /**
     * Evict all hierarchical cache
     * DELETE /api/v1/weather/hierarchical/cache/all
     */
    @DeleteMapping("/hierarchical/cache/all")
    public Mono<Map<String, Object>> evictAllHierarchicalCache() {
        return hierarchicalCacheService.evictAllCache()
                .map(success -> Map.<String, Object>of(
                    "success", success,
                    "message", success ? "All hierarchical cache evicted successfully" : "Failed to evict all hierarchical cache",
                    "timestamp", System.currentTimeMillis()
                ));
    }
} 