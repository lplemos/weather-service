package com.lplemos.weather_service.service.impl;

import com.lplemos.weather_service.exception.InvalidRequestException;
import com.lplemos.weather_service.integrations.weather.WeatherProvider;
import com.lplemos.weather_service.model.WeatherProviderType;
import com.lplemos.weather_service.model.WeatherResponse;
import com.lplemos.weather_service.model.WeatherSummary;
import com.lplemos.weather_service.service.WeatherService;
import com.lplemos.weather_service.service.WeatherServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeatherServiceImpl implements WeatherService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);
    private final List<WeatherProvider> weatherProviders;
    
    public WeatherServiceImpl(List<WeatherProvider> weatherProviders) {
        this.weatherProviders = weatherProviders;
        logger.info("{} WeatherServiceImpl initialized with {} providers: {}", 
            WeatherServiceConstants.LOG_PREFIX,
            weatherProviders.size(), 
            weatherProviders.stream().map(WeatherProvider::getProviderName).collect(Collectors.toList()));
    }
    
    // Método auxiliar para validar cidade
    private void validateCityName(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new InvalidRequestException("City name cannot be empty");
        }
        
        if (!cityName.matches("^[a-zA-ZÀ-ÿ\\s\\-']+$")) {
            throw new InvalidRequestException("City name can only contain letters, spaces, hyphens, and apostrophes");
        }
    }
    
    @Override
    @Cacheable(value = WeatherServiceConstants.CACHE_WEATHER_CURRENT, key = "#cityName + '-' + #providerType.name()")
    public Mono<Map<String, Object>> getCurrentWeather(String cityName, WeatherProviderType providerType) {
        validateCityName(cityName);
        
        WeatherProvider provider = getProvider(providerType);
        logger.info(WeatherServiceConstants.LOG_FETCHING_WEATHER, cityName);
        logger.info(WeatherServiceConstants.LOG_PROVIDER_SELECTED, 
            WeatherServiceConstants.LOG_PREFIX, providerType.getDisplayName(), cityName);
        
        return provider.getCurrentWeather(cityName)
                .doOnError(error -> {
                    logger.error(WeatherServiceConstants.LOG_ERROR_PROCESSING, 
                        WeatherServiceConstants.LOG_PREFIX, cityName, error.getMessage());
                });
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName) {
        return getCurrentWeather(cityName, WeatherProviderType.OPENWEATHERMAP);
    }
    
    @Override
    @Cacheable(value = WeatherServiceConstants.CACHE_WEATHER_CURRENT, key = "#cityName + '-structured-' + #providerType.name()")
    public Mono<WeatherResponse> getCurrentWeatherStructured(String cityName, WeatherProviderType providerType) {
        validateCityName(cityName);
        
        WeatherProvider provider = getProvider(providerType);
        logger.info(WeatherServiceConstants.LOG_FETCHING_WEATHER, cityName);
        logger.info(WeatherServiceConstants.LOG_PROVIDER_SELECTED, 
            WeatherServiceConstants.LOG_PREFIX, providerType.getDisplayName(), cityName);
        
        return provider.getCurrentWeatherStructured(cityName)
                .doOnError(error -> {
                    logger.error(WeatherServiceConstants.LOG_ERROR_PROCESSING, 
                        WeatherServiceConstants.LOG_PREFIX, cityName, error.getMessage());
                });
    }
    
    @Override
    public Mono<WeatherResponse> getCurrentWeatherStructured(String cityName) {
        return getCurrentWeatherStructured(cityName, WeatherProviderType.OPENWEATHERMAP);
    }
    
    @Override
    @Cacheable(value = WeatherServiceConstants.CACHE_WEATHER_CURRENT, key = "#cityName + '-summary-' + #providerType.name()")
    public Mono<WeatherSummary> getWeatherSummary(String cityName, WeatherProviderType providerType) {
        validateCityName(cityName);
        
        WeatherProvider provider = getProvider(providerType);
        logger.info(WeatherServiceConstants.LOG_FETCHING_WEATHER, cityName);
        logger.info(WeatherServiceConstants.LOG_PROVIDER_SELECTED, 
            WeatherServiceConstants.LOG_PREFIX, providerType.getDisplayName(), cityName);
        
        return provider.getWeatherSummary(cityName)
                .doOnError(error -> {
                    logger.error(WeatherServiceConstants.LOG_ERROR_PROCESSING, 
                        WeatherServiceConstants.LOG_PREFIX, cityName, error.getMessage());
                });
    }
    
    @Override
    public Mono<WeatherSummary> getWeatherSummary(String cityName) {
        return getWeatherSummary(cityName, WeatherProviderType.OPENWEATHERMAP);
    }
    
    @Override
    @Cacheable(value = WeatherServiceConstants.CACHE_WEATHER_FORECAST, key = "#cityName + '-' + #providerType.name()")
    public Mono<Map<String, Object>> getWeatherForecast(String cityName, WeatherProviderType providerType) {
        validateCityName(cityName);
        
        WeatherProvider provider = getProvider(providerType);
        logger.info(WeatherServiceConstants.LOG_FETCHING_FORECAST, cityName);
        logger.info(WeatherServiceConstants.LOG_PROVIDER_SELECTED, 
            WeatherServiceConstants.LOG_PREFIX, providerType.getDisplayName(), cityName);
        
        return provider.getWeatherForecast(cityName)
                .doOnError(error -> {
                    logger.error(WeatherServiceConstants.LOG_ERROR_PROCESSING, 
                        WeatherServiceConstants.LOG_PREFIX, cityName, error.getMessage());
                });
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName) {
        return getWeatherForecast(cityName, WeatherProviderType.OPENWEATHERMAP);
    }
    
    @Override
    @Cacheable(value = WeatherServiceConstants.CACHE_WEATHER_CURRENT, key = "'city-' + #cityId + '-' + #providerType.name()")
    public Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId, WeatherProviderType providerType) {
        if (cityId == null || cityId <= 0) {
            throw new InvalidRequestException("City ID must be a positive number");
        }
        
        WeatherProvider provider = getProvider(providerType);
        logger.info(WeatherServiceConstants.LOG_FETCHING_WEATHER, "City ID: " + cityId);
        logger.info(WeatherServiceConstants.LOG_PROVIDER_SELECTED, 
            WeatherServiceConstants.LOG_PREFIX, providerType.getDisplayName(), cityId);
        
        return provider.getCurrentWeatherById(cityId)
                .doOnError(error -> {
                    logger.error(WeatherServiceConstants.LOG_ERROR_PROCESSING, 
                        WeatherServiceConstants.LOG_PREFIX, cityId, error.getMessage());
                });
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId) {
        return getCurrentWeatherById(cityId, WeatherProviderType.OPENWEATHERMAP);
    }
    
    @Override
    public List<String> getAvailableProviders() {
        return weatherProviders.stream()
                .map(WeatherProvider::getProviderName)
                .collect(Collectors.toList());
    }
    
    @Override
    public Mono<Boolean> isProviderAvailable(WeatherProviderType providerType) {
        WeatherProvider provider = getProvider(providerType);
        if (provider == null) {
            return Mono.just(false);
        }
        return provider.isAvailable();
    }
    
    @Override
    public WeatherProvider getDefaultProvider() {
        return getProvider(WeatherProviderType.OPENWEATHERMAP);
    }
    
    private WeatherProvider getProvider(WeatherProviderType providerType) {
        return weatherProviders.stream()
                .filter(provider -> provider.getProviderName().equals(providerType.getDisplayName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format(WeatherServiceConstants.ERROR_PROVIDER_NOT_FOUND, providerType.getDisplayName())));
    }
} 