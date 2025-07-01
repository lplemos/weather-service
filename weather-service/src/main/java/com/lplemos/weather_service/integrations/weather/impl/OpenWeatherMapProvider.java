package com.lplemos.weather_service.integrations.weather.impl;

import com.lplemos.weather_service.config.WeatherApiConfig;
import com.lplemos.weather_service.integrations.weather.OpenWeatherMapConstants;
import com.lplemos.weather_service.integrations.weather.WeatherProvider;
import com.lplemos.weather_service.model.WeatherProviderType;
import com.lplemos.weather_service.model.WeatherResponse;
import com.lplemos.weather_service.model.WeatherSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class OpenWeatherMapProvider implements WeatherProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapProvider.class);
    private final WeatherApiConfig weatherApiConfig;
    private final WebClient webClient;
    
    public OpenWeatherMapProvider(WeatherApiConfig weatherApiConfig, WebClient.Builder webClientBuilder) {
        this.weatherApiConfig = weatherApiConfig;
        this.webClient = webClientBuilder
                .baseUrl(weatherApiConfig.getBaseUrl())
                .build();
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName) {
        return getCurrentWeather(cityName, weatherApiConfig.getLanguage());
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName, String language) {
        String safeRequestUrl = buildSafeWeatherUrl(cityName, language);
        logger.info(OpenWeatherMapConstants.LOG_MAKING_REQUEST, 
            OpenWeatherMapConstants.LOG_PREFIX, 
            OpenWeatherMapConstants.REQUEST_TYPE_CURRENT_WEATHER, 
            safeRequestUrl);
        
        // Log the actual URL being constructed (without API key for security)
        String actualUrl = String.format("%s%s?%s=%s&%s=%s&%s=%s&%s=%s",
                weatherApiConfig.getBaseUrl(),
                OpenWeatherMapConstants.WEATHER_ENDPOINT,
                OpenWeatherMapConstants.PARAM_CITY, cityName,
                OpenWeatherMapConstants.PARAM_API_KEY, "***MASKED***",
                OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits(),
                OpenWeatherMapConstants.PARAM_LANGUAGE, language);
        
        logger.info("{} Actual URL being constructed: {}", OpenWeatherMapConstants.LOG_PREFIX, actualUrl);
        logger.info("{} API Key configured: {}", OpenWeatherMapConstants.LOG_PREFIX, 
            weatherApiConfig.getApiKey() != null && !weatherApiConfig.getApiKey().isEmpty() ? "YES" : "NO");
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("weather")
                        .queryParam(OpenWeatherMapConstants.PARAM_CITY, cityName)
                        .queryParam(OpenWeatherMapConstants.PARAM_API_KEY, weatherApiConfig.getApiKey())
                        .queryParam(OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits())
                        .queryParam(OpenWeatherMapConstants.PARAM_LANGUAGE, language)
                        .build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> {
                    logger.info(OpenWeatherMapConstants.LOG_WEATHER_RECEIVED, 
                        OpenWeatherMapConstants.LOG_PREFIX, cityName, response.get("name"));
                })
                .doOnError(error -> {
                    logger.error(OpenWeatherMapConstants.LOG_ERROR_FETCHING, 
                        OpenWeatherMapConstants.LOG_PREFIX, 
                        OpenWeatherMapConstants.REQUEST_TYPE_CURRENT_WEATHER, 
                        cityName, error.getMessage());
                });
    }
    
    @Override
    public Mono<WeatherResponse> getCurrentWeatherStructured(String cityName) {
        return getCurrentWeatherStructured(cityName, weatherApiConfig.getLanguage());
    }
    
    @Override
    public Mono<WeatherResponse> getCurrentWeatherStructured(String cityName, String language) {
        String safeRequestUrl = buildSafeWeatherUrl(cityName, language);
        logger.info(OpenWeatherMapConstants.LOG_MAKING_REQUEST, 
            OpenWeatherMapConstants.LOG_PREFIX, 
            OpenWeatherMapConstants.REQUEST_TYPE_STRUCTURED_WEATHER, 
            safeRequestUrl);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(OpenWeatherMapConstants.WEATHER_ENDPOINT)
                        .queryParam(OpenWeatherMapConstants.PARAM_CITY, cityName)
                        .queryParam(OpenWeatherMapConstants.PARAM_API_KEY, weatherApiConfig.getApiKey())
                        .queryParam(OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits())
                        .queryParam(OpenWeatherMapConstants.PARAM_LANGUAGE, language)
                        .build())
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .doOnNext(response -> {
                    logger.info(OpenWeatherMapConstants.LOG_STRUCTURED_RECEIVED, 
                        OpenWeatherMapConstants.LOG_PREFIX, cityName, response.main().temp());
                })
                .doOnError(error -> {
                    logger.error(OpenWeatherMapConstants.LOG_ERROR_FETCHING, 
                        OpenWeatherMapConstants.LOG_PREFIX, 
                        OpenWeatherMapConstants.REQUEST_TYPE_STRUCTURED_WEATHER, 
                        cityName, error.getMessage());
                });
    }
    
    @Override
    public Mono<WeatherSummary> getWeatherSummary(String cityName) {
        return getWeatherSummary(cityName, weatherApiConfig.getLanguage());
    }
    
    @Override
    public Mono<WeatherSummary> getWeatherSummary(String cityName, String language) {
        return getCurrentWeatherStructured(cityName, language)
                .map(WeatherSummary::fromWeatherResponse)
                .doOnNext(summary -> {
                    logger.info(OpenWeatherMapConstants.LOG_SUMMARY_RECEIVED, 
                        OpenWeatherMapConstants.LOG_PREFIX, 
                        summary.city(), 
                        summary.getFormattedTemperature(), 
                        summary.description());
                });
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName) {
        return getWeatherForecast(cityName, weatherApiConfig.getLanguage());
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName, String language) {
        String safeRequestUrl = buildSafeForecastUrl(cityName, language);
        logger.info(OpenWeatherMapConstants.LOG_MAKING_REQUEST, 
            OpenWeatherMapConstants.LOG_PREFIX, 
            OpenWeatherMapConstants.REQUEST_TYPE_FORECAST, 
            safeRequestUrl);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(OpenWeatherMapConstants.FORECAST_ENDPOINT)
                        .queryParam(OpenWeatherMapConstants.PARAM_CITY, cityName)
                        .queryParam(OpenWeatherMapConstants.PARAM_API_KEY, weatherApiConfig.getApiKey())
                        .queryParam(OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits())
                        .queryParam(OpenWeatherMapConstants.PARAM_LANGUAGE, language)
                        .build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> {
                    logger.info(OpenWeatherMapConstants.LOG_FORECAST_RECEIVED, 
                        OpenWeatherMapConstants.LOG_PREFIX, cityName, response.get("city"));
                })
                .doOnError(error -> {
                    logger.error(OpenWeatherMapConstants.LOG_ERROR_FETCHING, 
                        OpenWeatherMapConstants.LOG_PREFIX, 
                        OpenWeatherMapConstants.REQUEST_TYPE_FORECAST, 
                        cityName, error.getMessage());
                });
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId) {
        return getCurrentWeatherById(cityId, weatherApiConfig.getLanguage());
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId, String language) {
        String safeRequestUrl = buildSafeWeatherByIdUrl(cityId, language);
        logger.info(OpenWeatherMapConstants.LOG_MAKING_REQUEST, 
            OpenWeatherMapConstants.LOG_PREFIX, 
            OpenWeatherMapConstants.REQUEST_TYPE_WEATHER_BY_ID, 
            safeRequestUrl);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(OpenWeatherMapConstants.WEATHER_ENDPOINT)
                        .queryParam(OpenWeatherMapConstants.PARAM_CITY_ID, cityId)
                        .queryParam(OpenWeatherMapConstants.PARAM_API_KEY, weatherApiConfig.getApiKey())
                        .queryParam(OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits())
                        .queryParam(OpenWeatherMapConstants.PARAM_LANGUAGE, language)
                        .build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> {
                    logger.info(OpenWeatherMapConstants.LOG_CITY_ID_RECEIVED, 
                        OpenWeatherMapConstants.LOG_PREFIX, cityId, response.get("name"));
                })
                .doOnError(error -> {
                    logger.error(OpenWeatherMapConstants.LOG_ERROR_FETCHING, 
                        OpenWeatherMapConstants.LOG_PREFIX, 
                        OpenWeatherMapConstants.REQUEST_TYPE_WEATHER_BY_ID, 
                        cityId, error.getMessage());
                });
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon) {
        return getCurrentWeatherByCoords(lat, lon, weatherApiConfig.getLanguage());
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, String language) {
        String safeRequestUrl = buildSafeWeatherByCoordsUrl(lat, lon, language);
        logger.info(OpenWeatherMapConstants.LOG_MAKING_REQUEST, 
            OpenWeatherMapConstants.LOG_PREFIX, 
            OpenWeatherMapConstants.REQUEST_TYPE_CURRENT_WEATHER, 
            safeRequestUrl);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(OpenWeatherMapConstants.WEATHER_ENDPOINT)
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam(OpenWeatherMapConstants.PARAM_API_KEY, weatherApiConfig.getApiKey())
                        .queryParam(OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits())
                        .queryParam(OpenWeatherMapConstants.PARAM_LANGUAGE, language)
                        .build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> {
                    logger.info(OpenWeatherMapConstants.LOG_WEATHER_RECEIVED, 
                        OpenWeatherMapConstants.LOG_PREFIX, String.format("(%.6f, %.6f)", lat, lon), response.get("name"));
                })
                .doOnError(error -> {
                    logger.error(OpenWeatherMapConstants.LOG_ERROR_FETCHING, 
                        OpenWeatherMapConstants.LOG_PREFIX, 
                        OpenWeatherMapConstants.REQUEST_TYPE_CURRENT_WEATHER, 
                        String.format("(%.6f, %.6f)", lat, lon), error.getMessage());
                });
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon) {
        return getWeatherForecastByCoords(lat, lon, weatherApiConfig.getLanguage());
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, String language) {
        String safeRequestUrl = buildSafeForecastByCoordsUrl(lat, lon, language);
        logger.info(OpenWeatherMapConstants.LOG_MAKING_REQUEST, 
            OpenWeatherMapConstants.LOG_PREFIX, 
            OpenWeatherMapConstants.REQUEST_TYPE_FORECAST, 
            safeRequestUrl);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(OpenWeatherMapConstants.FORECAST_ENDPOINT)
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam(OpenWeatherMapConstants.PARAM_API_KEY, weatherApiConfig.getApiKey())
                        .queryParam(OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits())
                        .queryParam(OpenWeatherMapConstants.PARAM_LANGUAGE, language)
                        .build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> {
                    logger.info(OpenWeatherMapConstants.LOG_FORECAST_RECEIVED, 
                        OpenWeatherMapConstants.LOG_PREFIX, String.format("(%.6f, %.6f)", lat, lon), response.get("city"));
                })
                .doOnError(error -> {
                    logger.error(OpenWeatherMapConstants.LOG_ERROR_FETCHING, 
                        OpenWeatherMapConstants.LOG_PREFIX, 
                        OpenWeatherMapConstants.REQUEST_TYPE_FORECAST, 
                        String.format("(%.6f, %.6f)", lat, lon), error.getMessage());
                });
    }
    
    @Override
    public String getProviderName() {
        return WeatherProviderType.OPENWEATHERMAP.getDisplayName();
    }
    
    @Override
    public Mono<Boolean> isAvailable() {
        // Simple health check - try to get weather for a known city
        return getCurrentWeather(OpenWeatherMapConstants.DEFAULT_HEALTH_CHECK_CITY)
                .map(response -> true)
                .onErrorReturn(false)
                .timeout(Duration.ofSeconds(OpenWeatherMapConstants.HEALTH_CHECK_TIMEOUT_SECONDS))
                .onErrorReturn(false);
    }
    
    /**
     * Builds a safe URL for logging (without API key)
     */
    private String buildSafeWeatherUrl(String cityName) {
        return buildSafeWeatherUrl(cityName, weatherApiConfig.getLanguage());
    }
    
    private String buildSafeWeatherUrl(String cityName, String language) {
        return String.format("%s%s?%s=%s&%s=***MASKED***&%s=%s&%s=%s",
                weatherApiConfig.getBaseUrl(),
                OpenWeatherMapConstants.WEATHER_ENDPOINT,
                OpenWeatherMapConstants.PARAM_CITY, cityName,
                OpenWeatherMapConstants.PARAM_API_KEY,
                OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits(),
                OpenWeatherMapConstants.PARAM_LANGUAGE, language);
    }
    
    /**
     * Builds a safe forecast URL for logging (without API key)
     */
    private String buildSafeForecastUrl(String cityName) {
        return buildSafeForecastUrl(cityName, weatherApiConfig.getLanguage());
    }
    
    private String buildSafeForecastUrl(String cityName, String language) {
        return String.format("%s%s?%s=%s&%s=***MASKED***&%s=%s&%s=%s",
                weatherApiConfig.getBaseUrl(),
                OpenWeatherMapConstants.FORECAST_ENDPOINT,
                OpenWeatherMapConstants.PARAM_CITY, cityName,
                OpenWeatherMapConstants.PARAM_API_KEY,
                OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits(),
                OpenWeatherMapConstants.PARAM_LANGUAGE, language);
    }
    
    /**
     * Builds a safe weather by ID URL for logging (without API key)
     */
    private String buildSafeWeatherByIdUrl(Integer cityId) {
        return buildSafeWeatherByIdUrl(cityId, weatherApiConfig.getLanguage());
    }
    
    private String buildSafeWeatherByIdUrl(Integer cityId, String language) {
        return String.format("%s%s?%s=%d&%s=***MASKED***&%s=%s&%s=%s",
                weatherApiConfig.getBaseUrl(),
                OpenWeatherMapConstants.WEATHER_ENDPOINT,
                OpenWeatherMapConstants.PARAM_CITY_ID, cityId,
                OpenWeatherMapConstants.PARAM_API_KEY,
                OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits(),
                OpenWeatherMapConstants.PARAM_LANGUAGE, language);
    }
    
    /**
     * Builds a safe weather by coordinates URL for logging (without API key)
     */
    private String buildSafeWeatherByCoordsUrl(Double lat, Double lon) {
        return buildSafeWeatherByCoordsUrl(lat, lon, weatherApiConfig.getLanguage());
    }
    
    private String buildSafeWeatherByCoordsUrl(Double lat, Double lon, String language) {
        return String.format("%s%s?%s=%s&%s=%s&%s=***MASKED***&%s=%s&%s=%s",
                weatherApiConfig.getBaseUrl(),
                OpenWeatherMapConstants.WEATHER_ENDPOINT,
                "lat", lat,
                "lon", lon,
                OpenWeatherMapConstants.PARAM_API_KEY,
                OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits(),
                OpenWeatherMapConstants.PARAM_LANGUAGE, language);
    }
    
    /**
     * Builds a safe forecast by coordinates URL for logging (without API key)
     */
    private String buildSafeForecastByCoordsUrl(Double lat, Double lon) {
        return buildSafeForecastByCoordsUrl(lat, lon, weatherApiConfig.getLanguage());
    }
    
    private String buildSafeForecastByCoordsUrl(Double lat, Double lon, String language) {
        return String.format("%s%s?%s=%s&%s=%s&%s=***MASKED***&%s=%s&%s=%s",
                weatherApiConfig.getBaseUrl(),
                OpenWeatherMapConstants.FORECAST_ENDPOINT,
                "lat", lat,
                "lon", lon,
                OpenWeatherMapConstants.PARAM_API_KEY,
                OpenWeatherMapConstants.PARAM_UNITS, weatherApiConfig.getUnits(),
                OpenWeatherMapConstants.PARAM_LANGUAGE, language);
    }
} 