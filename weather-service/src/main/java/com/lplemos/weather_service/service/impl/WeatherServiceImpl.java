package com.lplemos.weather_service.service.impl;

import com.lplemos.weather_service.config.WeatherApiConfig;
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
    private final WeatherApiConfig weatherApiConfig;
    
    public WeatherServiceImpl(List<WeatherProvider> weatherProviders, WeatherApiConfig weatherApiConfig) {
        this.weatherProviders = weatherProviders;
        this.weatherApiConfig = weatherApiConfig;
        logger.info("{} WeatherServiceImpl initialized with {} providers: {}", 
            WeatherServiceConstants.LOG_PREFIX,
            weatherProviders.size(), 
            weatherProviders.stream().map(WeatherProvider::getProviderName).collect(Collectors.toList()));
    }
    
    private Mono<Void> validateCityName(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return Mono.error(new InvalidRequestException("City name cannot be empty"));
        }
        
        return Mono.empty();
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName, WeatherProviderType providerType) {
        return getCurrentWeather(cityName, providerType, null);
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName, WeatherProviderType providerType, String language) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return Mono.error(new InvalidRequestException("City name cannot be empty"));
        }
        
        WeatherProvider provider = getProvider(providerType);
        
        // Use configured language if language parameter is null
        String finalLanguage = language != null ? language : weatherApiConfig.getLanguage();
        
        return provider.getCurrentWeather(cityName, finalLanguage);
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName) {
        return getCurrentWeather(cityName, getDefaultProviderType());
    }
    
    @Override
    public Mono<WeatherResponse> getCurrentWeatherStructured(String cityName, WeatherProviderType providerType) {
        return validateCityName(cityName)
                .then(Mono.just(cityName))
                .flatMap(name -> {
                    WeatherProvider provider = getProvider(providerType);
                    logger.info(WeatherServiceConstants.LOG_FETCHING_WEATHER, cityName);
                    logger.info(WeatherServiceConstants.LOG_PROVIDER_SELECTED, 
                        WeatherServiceConstants.LOG_PREFIX, providerType.getDisplayName(), cityName);
                    
                    return provider.getCurrentWeatherStructured(cityName, weatherApiConfig.getLanguage())
                            .doOnError(error -> {
                                logger.error(WeatherServiceConstants.LOG_ERROR_PROCESSING, 
                                    WeatherServiceConstants.LOG_PREFIX, cityName, error.getMessage());
                            });
                });
    }
    
    @Override
    public Mono<WeatherResponse> getCurrentWeatherStructured(String cityName) {
        return getCurrentWeatherStructured(cityName, getDefaultProviderType());
    }
    
    @Override
    public Mono<WeatherSummary> getWeatherSummary(String cityName, WeatherProviderType providerType) {
        return validateCityName(cityName)
                .then(Mono.just(cityName))
                .flatMap(name -> {
                    WeatherProvider provider = getProvider(providerType);
                    logger.info(WeatherServiceConstants.LOG_FETCHING_WEATHER, cityName);
                    logger.info(WeatherServiceConstants.LOG_PROVIDER_SELECTED, 
                        WeatherServiceConstants.LOG_PREFIX, providerType.getDisplayName(), cityName);
                    
                    return provider.getWeatherSummary(cityName, weatherApiConfig.getLanguage())
                            .doOnError(error -> {
                                logger.error(WeatherServiceConstants.LOG_ERROR_PROCESSING, 
                                    WeatherServiceConstants.LOG_PREFIX, cityName, error.getMessage());
                            });
                });
    }
    
    @Override
    public Mono<WeatherSummary> getWeatherSummary(String cityName) {
        return getWeatherSummary(cityName, getDefaultProviderType());
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName, WeatherProviderType providerType) {
        return getWeatherForecast(cityName, providerType, null);
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName, WeatherProviderType providerType, String language) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return Mono.error(new InvalidRequestException("City name cannot be empty"));
        }
        
        WeatherProvider provider = getProvider(providerType);
        
        // Use configured language if language parameter is null
        String finalLanguage = language != null ? language : weatherApiConfig.getLanguage();
        
        return provider.getWeatherForecast(cityName, finalLanguage);
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName) {
        return getWeatherForecast(cityName, getDefaultProviderType());
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId, WeatherProviderType providerType) {
        if (cityId == null || cityId <= 0) {
            return Mono.error(new InvalidRequestException("City ID must be a positive number"));
        }
        
        WeatherProvider provider = getProvider(providerType);
        logger.info(WeatherServiceConstants.LOG_FETCHING_WEATHER, "City ID: " + cityId);
        logger.info(WeatherServiceConstants.LOG_PROVIDER_SELECTED, 
            WeatherServiceConstants.LOG_PREFIX, providerType.getDisplayName(), cityId);
        
        return provider.getCurrentWeatherById(cityId, weatherApiConfig.getLanguage())
                .doOnError(error -> {
                    logger.error(WeatherServiceConstants.LOG_ERROR_PROCESSING, 
                        WeatherServiceConstants.LOG_PREFIX, cityId, error.getMessage());
                });
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId) {
        return getCurrentWeatherById(cityId, getDefaultProviderType());
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, WeatherProviderType providerType) {
        return getCurrentWeatherByCoords(lat, lon, providerType, null);
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, WeatherProviderType providerType, String language) {
        if (lat == null || lon == null) {
            return Mono.error(new InvalidRequestException("Latitude and longitude cannot be null"));
        }
        
        if (lat < -90 || lat > 90) {
            return Mono.error(new InvalidRequestException("Latitude must be between -90 and 90"));
        }
        
        if (lon < -180 || lon > 180) {
            return Mono.error(new InvalidRequestException("Longitude must be between -180 and 180"));
        }
        
        WeatherProvider provider = getProvider(providerType);
        
        // Use configured language if language parameter is null
        String finalLanguage = language != null ? language : weatherApiConfig.getLanguage();
        
        return provider.getCurrentWeatherByCoords(lat, lon, finalLanguage);
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon) {
        return getCurrentWeatherByCoords(lat, lon, getDefaultProviderType());
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, WeatherProviderType providerType) {
        return getWeatherForecastByCoords(lat, lon, providerType, null);
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, WeatherProviderType providerType, String language) {
        if (lat == null || lon == null) {
            return Mono.error(new InvalidRequestException("Latitude and longitude cannot be null"));
        }
        
        if (lat < -90 || lat > 90) {
            return Mono.error(new InvalidRequestException("Latitude must be between -90 and 90"));
        }
        
        if (lon < -180 || lon > 180) {
            return Mono.error(new InvalidRequestException("Longitude must be between -180 and 180"));
        }
        
        WeatherProvider provider = getProvider(providerType);
        
        // Use configured language if language parameter is null
        String finalLanguage = language != null ? language : weatherApiConfig.getLanguage();
        
        return provider.getWeatherForecastByCoords(lat, lon, finalLanguage);
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon) {
        return getWeatherForecastByCoords(lat, lon, getDefaultProviderType());
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
        return getProvider(getDefaultProviderType());
    }
    
    
    private WeatherProviderType getDefaultProviderType() {
        try {
            return WeatherProviderType.valueOf(weatherApiConfig.getDefaultProvider());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid default provider configured: {}. Falling back to OPENWEATHERMAP", 
                weatherApiConfig.getDefaultProvider());
            return WeatherProviderType.OPENWEATHERMAP;
        }
    }
    
    @Override
    public WeatherProvider getProvider(WeatherProviderType providerType) {
        return weatherProviders.stream()
                .filter(provider -> provider.getProviderName().equals(providerType.getDisplayName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format(WeatherServiceConstants.ERROR_PROVIDER_NOT_FOUND, providerType.getDisplayName())));
    }
} 